package com.batgm.handledata.filter;

import com.batgm.handledata.config.InterceptorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * @author yqq
 * @createdate 2020/7/23
 */
@WebFilter(urlPatterns = "/user/*", filterName = "logCostFilter")
public class LogCostFilter implements Filter {
    private static Logger logger = LoggerFactory.getLogger(LogCostFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        long start = System.currentTimeMillis();
        filterChain.doFilter(servletRequest, servletResponse);
        logger.info("logCostFilter Execute cost=" + (System.currentTimeMillis() - start));
    }

    @Override
    public void destroy() {

    }
}
