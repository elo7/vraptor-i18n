package br.com.caelum.vraptor.i18n;

import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.collect.ForwardingMap;

@Named("l")
@RequestScoped
public class LocalizedFormatter extends ForwardingMap<Class<?>, Object> {

	private final ResourceBundle bundle;
	private final Locale locale;

	/**
	 * @deprecated CDI eyes only
	 */
	public LocalizedFormatter() {
		this(null, null);
	}

	@Inject
	public LocalizedFormatter(ResourceBundle bundle, Locale locale) {
		this.bundle = bundle;
		this.locale = locale;
	}

	@Override
	public LocalizedInfo get(Object key) {
		if (key instanceof Calendar) {
			Date date = ((Calendar) key).getTime();
			return new LocalizedData(date, bundle, locale);
		} else if (key instanceof Date) {
			return new LocalizedData((Date) key, bundle, locale);
		} else if (isJodaTime(key)) {
			Date date = convertJodaTime(key);
			return new LocalizedData(date, bundle, locale);
		} else if (key instanceof Number) {
			return new LocalizedNumber((Number) key, bundle, locale);
		} else {
			throw new IllegalArgumentException(
					"Cannot format given Object as a Date");
		}
	}

	private boolean isJodaTime(Object key) {
		try {
			Class<?> clazz = Class
					.forName("org.joda.time.base.AbstractInstant");
			return clazz.isAssignableFrom(key.getClass());
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	private Date convertJodaTime(Object key) {
		try {
			return (Date) key.getClass().getMethod("toDate").invoke(key);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * All methods from {@link Map} that were not override by
	 * {@link LocalizedFormatter} will call {@link #delegate()} This way all
	 * methods that were not override will throw
	 * {@link UnsupportedOperationException}
	 */
	@Override
	protected Map<Class<?>, Object> delegate() {
		throw new UnsupportedOperationException(
				"A i18n localized formatter does not support this method");
	}
}
