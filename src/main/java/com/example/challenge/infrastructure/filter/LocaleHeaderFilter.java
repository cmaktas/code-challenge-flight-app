package com.example.challenge.infrastructure.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

/**
 * Alternative locale resolver filter that sets the locale based on the "Accept-Language" header.
 */
@RequiredArgsConstructor
@Component
public class LocaleHeaderFilter implements Filter {

    private final LocaleResolver localeResolver;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String acceptLanguage = httpRequest.getHeader("Accept-Language");

            if (acceptLanguage != null && !acceptLanguage.isEmpty()) {
                Locale locale = Locale.forLanguageTag(acceptLanguage);
                localeResolver.setLocale(httpRequest, (HttpServletResponse) response, locale);
                LocaleContextHolder.setLocale(locale);
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            LocaleContextHolder.resetLocaleContext();
        }
    }
}
