package com.aiapp.auth.resolver;

import com.aiapp.user.entity.Role;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface UserId {
    boolean required() default true;
    Role[] allowedRoles() default {Role.MEMBER, Role.ADMIN};
}
