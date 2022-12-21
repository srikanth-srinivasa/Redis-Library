package net.apmoller.crb.telikos.microservices.cache.library.util;

import com.fasterxml.jackson.core.type.TypeReference;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class AspectUtils {

    private final ExpressionParser expressionParser = new SpelExpressionParser();

    public String getKeyVal(JoinPoint joinPoint, String key) {
        String cacheKey = resolveKey(joinPoint, key);
            return cacheKey;

    }

    private String resolveKey(JoinPoint joinPoint, String key) {
        if (StringUtils.hasText(key)) {
            if (key.contains("#") || key.contains("'")) {
                String[] parameterNames = getParamNames(joinPoint);
                Object[] args = joinPoint.getArgs();
                StandardEvaluationContext context = new StandardEvaluationContext();
                for (int i = 0; i < parameterNames.length; i++) {
                    context.setVariable(parameterNames[i], args[i]);
                }
                return (String) expressionParser.parseExpression(key).getValue(context);
            }
            return key;
        }
        throw new RuntimeException("RedisReactiveCache annotation missing key");
    }

    public Method getMethod(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        return methodSignature.getMethod();
    }

    private String[] getParamNames(JoinPoint joinPoint) {
        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
        return codeSignature.getParameterNames();
    }


    public Type getMethodActualReturnType(Method method) {
        return ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
    }

    public TypeReference getTypeReference(Method method) {
        return new TypeReference<Object>() {
            @Override
            public Type getType() {
                return getMethodActualReturnType(method);
            }
        };
    }



}
