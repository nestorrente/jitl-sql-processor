package com.nestorrente.jitl.module.sql;

import java.io.Closeable;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;

import com.nestorrente.jitl.module.sql.accessor.index.IndexAccessor;
import com.nestorrente.jitl.module.sql.accessor.property.PropertyAccessor;
import com.nestorrente.jitl.util.ReflectionUtils;
import com.nestorrente.jitl.util.StringUtils;

class QueryParser implements Closeable {

	private static final int EOF = -1;
	private static final char SCAPE_CHAR = '\\';
	private static final char PARAMETER_START_CHAR = ':';
	private static final char KEY_ACCESSOR_START_CHAR = '.';
	private static final char INDEX_ACCESSOR_START_CHAR = '[';
	private static final char INDEX_ACCESSOR_END_CHAR = ']';

	public static Pair<String, Collection<Object>> parse(SQLModule module, String query, Map<String, Object> parameters) {
		try(QueryParser queryParser = new QueryParser(module, query, parameters)) {
			return queryParser.parse();
		}
	}

	private final SQLModule module;
	private final PushbackReader reader;
	private final Map<String, Object> inputParameters;
	private final StringBuilder builder;
	private final Collection<Object> outputParameters;

	private QueryParser(SQLModule module, String query, Map<String, Object> parameters) {
		this.module = module;
		this.reader = new PushbackReader(new StringReader(query));
		this.inputParameters = parameters;
		this.builder = new StringBuilder();
		this.outputParameters = new ArrayList<>();
	}

	public Pair<String, Collection<Object>> parse() {
		this.readChunk();
		return Pair.of(this.builder.toString(), this.outputParameters);
	}

	private int readChar() {
		try {
			return this.reader.read();
		} catch(IOException ex) {
			// TODO replace with a custom exception (RuntimeIOException?)
			throw new RuntimeException(ex);
		}
	}

	private void unreadChar(int ch) {
		try {
			this.reader.unread(ch);
		} catch(IOException ex) {
			// TODO replace with a custom exception (RuntimeIOException?)
			throw new RuntimeException(ex);
		}
	}

	private void readChunk() {

		while(true) {

			int ch = this.readChar();

			switch(ch) {
				case EOF:
					return;
				case SCAPE_CHAR:
					this.processEscaped();
					break;
				case PARAMETER_START_CHAR:
					this.processParameter();
					break;
				default:
					this.builder.append((char) ch);
					break;
			}

		}

	}

	private void processEscaped() {

		int ch = this.readChar();

		// Ensure this is a valid escape sequence
		if(ch != SCAPE_CHAR && ch != PARAMETER_START_CHAR && ch != KEY_ACCESSOR_START_CHAR && ch != INDEX_ACCESSOR_START_CHAR && ch != INDEX_ACCESSOR_END_CHAR) {
			// TODO replace with a custom exception (SyntaxErrorException?)
			throw new RuntimeException(String.format(
				"Invalid escape sequence. Valid ones are: %c%c, %c%c, %c%c, %c%c and %c%c",
				SCAPE_CHAR, SCAPE_CHAR,
				SCAPE_CHAR, PARAMETER_START_CHAR,
				SCAPE_CHAR, KEY_ACCESSOR_START_CHAR,
				SCAPE_CHAR, INDEX_ACCESSOR_START_CHAR,
				SCAPE_CHAR, INDEX_ACCESSOR_END_CHAR));
		}

		this.builder.append((char) ch);

	}

	private void processParameter() {

		Object value = this.inputParameters.get(this.readNameOrKey());

		while(true) {

			int ch = this.readChar();

			if(ch == KEY_ACCESSOR_START_CHAR) {

				String key = this.readNameOrKey();

				Objects.requireNonNull(value);

				@SuppressWarnings("unchecked")
				PropertyAccessor<Object> accessor = (PropertyAccessor<Object>) this.module.getPropertyAccessor(value.getClass());

				if(accessor == null) {
					// TODO replace with a better exception
					throw new RuntimeException(String.format("Cannot use property access in type %s", value.getClass().getName()));
				}

				value = accessor.access(value, key);

			} else if(ch == INDEX_ACCESSOR_START_CHAR) {

				int index = this.readIndex();

				Objects.requireNonNull(value);

				@SuppressWarnings("unchecked")
				IndexAccessor<Object> accessor = (IndexAccessor<Object>) this.module.getIndexAccessor(value.getClass());

				if(accessor == null) {
					// TODO replace with a better exception
					throw new RuntimeException(String.format("Cannot use index access in type %s", value.getClass().getName()));
				}

				value = accessor.access(value, index);

			} else {
				this.unreadChar(ch);
				break;
			}

		}

		this.addParameterToQuery(value);

	}

	// FIXME this is mocked
	// TODO add some kind of "type resolver"s? They will receive an object then they will return a Collection of objects that must be interpreted as multiple question marks ("?")
	private void addParameterToQuery(Object value) {

		if(value instanceof Collection) {

			this.outputParameters.addAll((Collection<?>) value);

			this.builder.append(StringUtils.joinRepeating("?", ", ", ((Collection<?>) value).size()));

		} else if(value instanceof Map) {

			this.outputParameters.addAll(((Map<?, ?>) value).values());

			this.builder.append(StringUtils.joinRepeating("?", ", ", ((Map<?, ?>) value).size()));

		} else if(ReflectionUtils.isArray(value)) {

			ReflectionUtils.addAllFromArray(this.outputParameters, value);

			this.builder.append(StringUtils.joinRepeating("?", ", ", Array.getLength(value)));

		} else {

			// When the value isn't a collection, a map nor an array, we treat it as a single value
			this.outputParameters.add(value);
			this.builder.append('?');

		}

	}

	private String readNameOrKey() {

		int ch = this.readChar();

		if(ch == EOF) {
			throw new IllegalStateException("Expected identifier start but was EOF");
		}

		if(!Character.isJavaIdentifierStart(ch)) {
			throw new IllegalStateException(String.format("Expected identifier start but was %c", (char) ch));
		}

		StringBuilder identifierBuilder = new StringBuilder().append((char) ch);

		while(true) {

			ch = this.readChar();

			if(ch == EOF) {
				break;
			}

			if(!Character.isJavaIdentifierPart(ch)) {
				this.unreadChar(ch);
				break;
			}

			identifierBuilder.append((char) ch);

		}

		return identifierBuilder.toString();

	}

	private int readIndex() {

		int ch = this.readChar();

		if(ch == EOF) {
			throw new IllegalStateException("Expected digit but was EOF");
		}

		if(ch < '0' || ch > '9') {
			throw new IllegalStateException(String.format("Expected index but was %c", (char) ch));
		}

		StringBuilder indexBuilder = new StringBuilder().append((char) ch);

		while(true) {

			ch = this.readChar();

			if(ch == EOF) {
				break;
			}

			if(ch >= '0' && ch <= '9') {
				indexBuilder.append((char) ch);
			} else if(ch == INDEX_ACCESSOR_END_CHAR) {
				break;
			} else {
				throw new IllegalStateException(String.format("Expected digit or ] but was %c", (char) ch));
			}

		}

		return Integer.parseInt(indexBuilder.toString());

	}

	@Override
	public void close() {
		try {
			this.reader.close();
		} catch(IOException ex) {
			// TODO replace with a custom exception (RuntimeIOException?)
			throw new RuntimeException(ex);
		}
	}

}
