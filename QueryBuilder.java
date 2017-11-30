package com.vistana.onsiteconcierge.core.service.impl;

import static com.vistana.onsiteconcierge.core.CoreConstants.BRACKET_LEFT;
import static com.vistana.onsiteconcierge.core.CoreConstants.BRACKET_RIGHT;
import static com.vistana.onsiteconcierge.core.CoreConstants.PARENTHESIS_LEFT;
import static com.vistana.onsiteconcierge.core.CoreConstants.PARENTHESIS_RIGHT;
import static com.vistana.onsiteconcierge.core.CoreConstants.PERIOD;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQuery;

public class QueryBuilder {

	private static final Map<String, String> ALLOCATION_CATEGORIES = new HashMap<>();

	private static final List<String> ALLOCATION_SORTING = new ArrayList<>();

	static {
		ALLOCATION_CATEGORIES.put("guestTypes", "guestTypeCode(==)");
		ALLOCATION_CATEGORIES.put("leadStatuses", "leadStatusCode(==)");
		ALLOCATION_SORTING.add("guest.lastName(asc)");
		ALLOCATION_SORTING.add("guest.firstName(asc)");
		ALLOCATION_SORTING.add("arrivalDate(asc)");
	}

	private Path<?> alias;

	private EntityPathBase<?> base;

	private BooleanExpression expressions;

	private Map<String, QueryBuilder> joins = new HashMap<>();

	private JPAQuery<?> query;

	/**
	 * This stores the base where all queries are started from.
	 *
	 * @param query
	 *            - {@link JPAQuery}
	 * @param base
	 *            - {@link EntityPathBase}
	 */
	public QueryBuilder(JPAQuery<?> query, EntityPathBase<?> base) {
		this.base = base;
		this.query = query;
	}

	/**
	 * This allows us to use alias otherwise multiple joins are created on the same table
	 *
	 * For example: lead.guest.room where lead.guest.room.roomName = "Lockoff" inner join Guest
	 * guest1 inner join Guest guest2 inner join Room room1
	 *
	 * For example: lead.guest.room order by lead.guest.room.roomName.asc() inner join Guest guest1
	 * inner join Guest guest2 inner join Room room1
	 *
	 * @param query
	 *            - {@link JPAQuery}
	 * @param base
	 *            - {@link EntityPathBase}Joined expression (lead.guest)
	 * @param pathAlias
	 *            - {@link Path} alias (new Guest("Guest")
	 */
	public QueryBuilder(JPAQuery<?> query, EntityPathBase<?> base, Path<?> pathAlias) {
		this.base = base;
		this.query = query;
		this.alias = pathAlias;
	}

	@SuppressWarnings("rawtypes")
	private BooleanExpression applyExpression(EntityPathBase<?> base, String queryPath, List<?> values) {

		String path = getTextLeft(queryPath);
		String comparator = getTextLeftRight(queryPath);

		ComparableExpressionBase<?> expr = findField(ComparableExpressionBase.class, getAliasOrBase(), path);
		Class<?> type = expr.getType();

		BooleanExpression ors = null;
		for (Object value : values) {
			BooleanExpression expression = null;
			if (type.equals(String.class)) {
				expression = applyExpressionString(base, (StringPath) expr, comparator, (String) value);
			} else if (Number.class.isAssignableFrom(type)) {
				expression = applyExpressionNumber(base, (NumberPath) expr, comparator, (Number) value);
			} else if (type.equals(Date.class)) {
				expression = applyExpressionDate(base, (DateTimePath) expr, comparator, (Date) value);
			} else {
				throw new IllegalStateException(
						"Error applying expression: " + base + PERIOD + path + PERIOD + comparator);
			}

			if (ors == null) {
				ors = expression;
			} else {
				ors = ors.or(expression);
			}
		}

		return ors;
	}

	/**
	 * Parses string to create joins and adds where expressions.
	 *
	 * For example: LeadContact(base) and queryPath "guest(innerJoin).room(innerJoin).roomName(==)"
	 *
	 * @param queryPath
	 *            - Query expressions
	 * @param values
	 *            - Values to apply to field
	 */
	public void applyExpression(String queryPath, List<?> values) {

		if (queryPath.contains(PERIOD)) {
			QueryBuilder join = applyJoinTable(query, base, queryPath);

			int index = queryPath.indexOf(PERIOD);
			String remainingPath = queryPath.substring(index + 1);
			join.applyExpression(remainingPath, values);
		} else {
			BooleanExpression ors = applyExpression(base, queryPath, values);

			if (expressions == null) {
				expressions = ors;
			} else {
				expressions = expressions.and(ors);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private BooleanExpression applyExpressionDate(EntityPathBase<?> base, DateTimePath expr, String comparator,
			Date value) {

		BooleanExpression expression = null;
		switch (comparator) {
		case "==": {
			expression = expr.eq(value);
			break;
		}
		case "<=": {
			expression = expr.loe(value);
			break;
		}
		case "<": {
			expression = expr.lt(value);
			break;
		}
		case ">=": {
			expression = expr.goe(value);
			break;
		}
		case ">": {
			expression = expr.gt(value);
			break;
		}
		default: {
			throw new IllegalStateException(
					"Error applying date expression: " + base + PERIOD + expr + PERIOD + comparator);
		}
		}
		return expression;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private BooleanExpression applyExpressionNumber(EntityPathBase<?> base, NumberPath expr, String comparator,
			Number value) {

		BooleanExpression expression = null;
		switch (comparator) {
		case "==": {
			expression = expr.eq(value);
			break;
		}
		case "!=": {
			expression = expr.ne(value);
			break;
		}
		case "<=": {
			expression = expr.loe(value);
			break;
		}
		case "<": {
			expression = expr.lt(value);
			break;
		}
		case ">=": {
			expression = expr.goe(value);
			break;
		}
		case ">": {
			expression = expr.gt(value);
			break;
		}
		default: {
			throw new IllegalStateException(
					"Error applying number expression: " + base + PERIOD + expr + PERIOD + comparator);
		}
		}
		return expression;
	}

	private BooleanExpression applyExpressionString(EntityPathBase<?> base, StringPath expr, String comparator,
			String value) {

		BooleanExpression expression = null;
		switch (comparator) {
		case "==": {
			expression = expr.eq(value);
			break;
		}
		case "!=": {
			expression = expr.ne(value);
			break;
		}
		case "like": {
			expression = expr.like(value);
			break;
		}
		default: {
			throw new IllegalStateException(
					"Error applying string expression: " + base + PERIOD + expr + PERIOD + comparator);
		}
		}
		return expression;
	}

	@SuppressWarnings("unchecked")
	private <P> QueryBuilder applyJoinTable(JPAQuery<?> query, EntityPathBase<?> base, String queryPath) {

		String path = getTextLeft(queryPath);
		String comparator = getTextLeftRight(queryPath);

		QueryBuilder join = joins.get(path);
		if (join == null) {
			EntityPathBase<P> joinTable = findField(EntityPathBase.class, getAliasOrBase(), path);
			Path<P> pathAlias = createInstance(Path.class, joinTable.getClass());
			applyJoinTable(query, joinTable, pathAlias, comparator);

			join = new QueryBuilder(query, joinTable, pathAlias);
			joins.put(path, join);
		}
		return join;
	}

	private <P> void applyJoinTable(JPAQuery<?> query, EntityPathBase<P> joinTable, Path<P> pathAlias,
			String comparator) {

		switch (comparator) {
		case "innerJoin": {
			query = query.innerJoin(joinTable, pathAlias);
			break;
		}
		case "leftJoin": {
			query = query.leftJoin(joinTable, pathAlias);
			break;
		}
		case "rightJoin": {
			query = query.rightJoin(joinTable, pathAlias);
			break;
		}
		default: {
			// only handles @OneToOne / @ManyToOne relationships
			throw new IllegalStateException("Error joining table: " + base + PERIOD + joinTable + PERIOD + comparator);
		}
		}
	}

	/**
	 * Parses string to create joins.
	 *
	 * For example: LeadContact(base) and queryPath "guest(innerJoin).room(innerJoin)"
	 *
	 * @param queryPath
	 *            - Query expressions
	 */
	public void applyJoinTable(String queryPath) {

		if (queryPath.contains(PERIOD)) {
			QueryBuilder join = applyJoinTable(query, base, queryPath);

			int index = queryPath.indexOf(PERIOD);
			String remainingPath = queryPath.substring(index + 1);
			join.applyJoinTable(remainingPath);
		} else {
			applyJoinTable(query, base, queryPath);
		}
	}

	/**
	 * Parses string to create sorting expression.
	 *
	 * For example: LeadContact(base) and sorting "guest.lastName(asc)"
	 *
	 * @param sortOrder
	 *            - Sorting expressions
	 * @return {@link OrderSpecifier}
	 */
	public OrderSpecifier<?> applySortOrder(String sortOrder) {

		if (sortOrder.contains(PERIOD)) {
			int index = sortOrder.indexOf(PERIOD);
			String prefix = sortOrder.substring(0, index);

			QueryBuilder join = joins.get(prefix);
			if (join == null) {
				throw new IllegalStateException("Error applying sortOrder: " + base + PERIOD + sortOrder);
			}

			String remainingPath = sortOrder.substring(index + 1);
			return join.applySortOrder(remainingPath);
		} else {
			String path = getTextLeft(sortOrder);
			String order = getTextLeftRight(sortOrder);
			ComparableExpressionBase<?> expr = findField(ComparableExpressionBase.class, getAliasOrBase(), path);
			return applySortOrder(path, expr, order);
		}
	}

	private OrderSpecifier<?> applySortOrder(String path, ComparableExpressionBase<?> expr, String order) {

		OrderSpecifier<?> sort = null;
		switch (order) {
		case "asc": {
			sort = expr.asc();
			break;
		}
		case "desc": {
			sort = expr.desc();
			break;
		}
		default: {
			throw new IllegalStateException(
					"Error applying sortOrder expression: " + base + PERIOD + path + PERIOD + order);
		}
		}

		return sort;
	}

	@SuppressWarnings("unchecked")
	private <T> T createInstance(Class<T> type, Class<?> clazz) {

		T newInstance = null;
		try {
			newInstance = (T) clazz.getDeclaredConstructor(String.class)
					.newInstance(clazz.getSimpleName().toUpperCase());
		} catch (Exception e) {
			throw new IllegalStateException("Error creating instance: " + clazz);
		}
		return newInstance;
	}

	@SuppressWarnings("unchecked")
	private <T extends Object> T findField(Class<T> clazz, Object base, String path) {

		T entity = null;
		try {
			if (path.contains(BRACKET_LEFT)) {
				String outer = getTextLeftRightBracket(path);
				path = getTextRightSuffixBracket(path);

				Field field = base.getClass().getDeclaredField(outer);
				base = field.get(base);
			}

			Field field = base.getClass().getDeclaredField(path);
			entity = (T) field.get(base);
		} catch (Exception e) {
			// only searches one deep for example id[organizationId]
			throw new IllegalStateException("Error finding field: " + base + PERIOD + path);
		}
		return entity;
	}

	/**
	 * Return alias.
	 *
	 * @return {@link Path}
	 */
	public Path<?> getAlias() {

		return alias;
	}

	/**
	 * Retrieves alias recursively.
	 *
	 * @return {@link EntityPathBase}
	 */
	public QueryBuilder getAlias(String alias) {

		int index = alias.indexOf(PERIOD);
		if (alias.contains(PERIOD)) {
			String prefix = alias.substring(0, index);
			QueryBuilder nested = joins.get(prefix);

			String remainingPath = alias.substring(index + 1);
			return nested.getAlias(remainingPath);
		} else {
			return joins.get(alias);
		}
	}

	private Path<?> getAliasOrBase() {

		if (alias == null) {
			return base;
		} else {
			return alias;
		}
	}

	/**
	 * Retrieves where clause expressions recursively.
	 *
	 * @return {@link BooleanExpression}
	 */
	public BooleanExpression getExpressions() {

		BooleanExpression expression = expressions;
		for (QueryBuilder builder : joins.values()) {
			BooleanExpression nested = builder.getExpressions();
			if (expression == null) {
				expression = nested;
			} else {
				expression = expressions.and(nested);
			}
		}
		return expression;
	}

	private String getTextLeft(String text) {

		int left = text.indexOf(PARENTHESIS_LEFT);

		if (left < 0) {
			throw new IllegalStateException("Error retrieving text (left): " + text);
		}
		return text.substring(0, left);
	}

	private String getTextLeftRight(String text) {

		int left = text.indexOf(PARENTHESIS_LEFT) + 1;
		int right = text.indexOf(PARENTHESIS_RIGHT);

		if (left < 0 || right < left) {
			throw new IllegalStateException("Error retrieving text (left right): " + text);
		}
		return text.substring(left, right);
	}

	private String getTextLeftRightBracket(String text) {

		int left = text.indexOf(BRACKET_LEFT) + 1;
		int right = text.indexOf(BRACKET_RIGHT);

		if (left < 0 || right < left) {
			throw new IllegalStateException("Error retrieving text [left right]: " + text);
		}
		return text.substring(left, right);
	}

	private String getTextRightSuffixBracket(String text) {

		int right = text.indexOf(BRACKET_RIGHT) + 1;

		if (right < 0) {
			throw new IllegalStateException("Error retrieving text [right suffix]: " + right);
		}
		return text.substring(right);
	}

}
