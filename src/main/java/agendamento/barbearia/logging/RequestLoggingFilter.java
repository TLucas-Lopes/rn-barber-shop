package agendamento.barbearia.logging;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        long ini = System.currentTimeMillis();

        String method = request.getMethod();
        String path = request.getRequestURI();
        String query = request.getQueryString();

        // opcional: id de request pra rastrear
        String rid = java.util.UUID.randomUUID().toString().substring(0, 8);
        MDC.put("rid", rid);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long ms = System.currentTimeMillis() - ini;
            int status = response.getStatus();

            String fullPath = (query == null ? path : path + "?" + query);

            if (status >= 500) {
                log.error("RID={} {} {} -> {} ({}ms)", rid, method, fullPath, status, ms);
            } else if (status >= 400) {
                log.warn("RID={} {} {} -> {} ({}ms)", rid, method, fullPath, status, ms);
            } else {
                log.info("RID={} {} {} -> {} ({}ms)", rid, method, fullPath, status, ms);
            }

            MDC.remove("rid");
        }
    }
}

