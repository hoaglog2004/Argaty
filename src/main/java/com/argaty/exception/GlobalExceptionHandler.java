package com.argaty.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.argaty.dto.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for all controllers
 * Handles errors and provides meaningful error pages and responses
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle ResourceNotFoundException (404)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Object handleResourceNotFoundException(
            ResourceNotFoundException ex, 
            HttpServletRequest request) {
        
        log.error("Resource not found: {}", ex.getMessage());
        
        // If it's an API request, return JSON
        if (isApiRequest(request)) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
        }
        
        // Otherwise return error page
        ModelAndView mav = new ModelAndView("error/error");
        mav.addObject("errorCode", 404);
        mav.addObject("errorTitle", "Không tìm thấy");
        mav.addObject("errorMessage", ex.getMessage());
        return mav;
    }

    /**
     * Handle UnauthorizedException (401)
     */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Object handleUnauthorizedException(
            UnauthorizedException ex, 
            HttpServletRequest request) {
        
        log.error("Unauthorized access: {}", ex.getMessage());
        
        if (isApiRequest(request)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage()));
        }
        
        // Redirect to login page
        return "redirect:/auth/login?error=unauthorized";
    }

    /**
     * Handle ForbiddenException and AccessDeniedException (403)
     */
    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Object handleForbiddenException(
            Exception ex, 
            HttpServletRequest request) {
        
        log.error("Access denied: {}", ex.getMessage());
        
        if (isApiRequest(request)) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Bạn không có quyền truy cập tài nguyên này"));
        }
        
        ModelAndView mav = new ModelAndView("error/error");
        mav.addObject("errorCode", 403);
        mav.addObject("errorTitle", "Truy cập bị từ chối");
        mav.addObject("errorMessage", "Bạn không có quyền truy cập tài nguyên này");
        return mav;
    }

    /**
     * Handle BadRequestException (400)
     */
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Object handleBadRequestException(
            BadRequestException ex, 
            HttpServletRequest request) {
        
        log.error("Bad request: {}", ex.getMessage());
        
        if (isApiRequest(request)) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
        }
        
        ModelAndView mav = new ModelAndView("error/error");
        mav.addObject("errorCode", 400);
        mav.addObject("errorTitle", "Yêu cầu không hợp lệ");
        mav.addObject("errorMessage", ex.getMessage());
        return mav;
    }

    /**
     * Handle NoHandlerFoundException (404)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Object handleNoHandlerFound(
            NoHandlerFoundException ex, 
            HttpServletRequest request) {
        
        // Nếu là request static files, trả về 404 đơn giản
        if (isStaticResourceRequest(request)) {
            return ResponseEntity.notFound().build();
        }
        
        log.error("No handler found: {}", ex.getMessage());
        
        ModelAndView mav = new ModelAndView("error/error");
        mav.addObject("errorCode", 404);
        mav.addObject("errorTitle", "Không tìm thấy trang");
        mav.addObject("errorMessage", "Trang bạn đang tìm kiếm không tồn tại");
        return mav;
    }

    /**
     * Handle missing static resources (404), e.g. /uploads/**
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Object handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request) {

        // For static resources, return a clean 404 (avoid noisy 500 logs)
        if (isStaticResourceRequest(request)) {
            return ResponseEntity.notFound().build();
        }

        if (isApiRequest(request)) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Không tìm thấy tài nguyên"));
        }

        ModelAndView mav = new ModelAndView("error/error");
        mav.addObject("errorCode", 404);
        mav.addObject("errorTitle", "Không tìm thấy");
        mav.addObject("errorMessage", "Tài nguyên bạn yêu cầu không tồn tại");
        return mav;
    }

    /**
     * Handle all other exceptions (500)
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Object handleGeneralException(
            Exception ex, 
            HttpServletRequest request) {
        
        log.error("Internal server error: ", ex);
        
        // Nếu là request static files, trả về 500 đơn giản
        if (isStaticResourceRequest(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
        if (isApiRequest(request)) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau."));
        }
        
        ModelAndView mav = new ModelAndView("error/error");
        mav.addObject("errorCode", 500);
        mav.addObject("errorTitle", "Lỗi hệ thống");
        mav.addObject("errorMessage", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.");
        return mav;
    }

    /**
     * Check if the request is an API request
     */
    private boolean isApiRequest(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        return requestUri.startsWith("/api/");
    }

    /**
     * Check if the request is for static resources
     */
    private boolean isStaticResourceRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/css/") || 
               uri.startsWith("/js/") || 
               uri.startsWith("/images/") || 
               uri.startsWith("/uploads/") || 
               uri.startsWith("/static/") ||
               uri.endsWith(".css") ||
               uri.endsWith(".js") ||
               uri.endsWith(".svg") ||
               uri.endsWith(".webp") ||
               uri.endsWith(".png") ||
               uri.endsWith(".jpg") ||
               uri.endsWith(".jpeg") ||
               uri.endsWith(".ico");
    }
}
