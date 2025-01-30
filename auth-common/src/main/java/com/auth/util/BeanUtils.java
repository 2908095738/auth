package com.auth.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Bean 工具类
 *
 * 1. 默认使用 {@link BeanUtil} 作为实现类，虽然不同 bean 工具的性能有差别，但是对绝大多数同学的项目，不用在意这点性能
 * 2. 针对复杂的对象转换，可以搜参考 AuthConvert 实现，通过 mapstruct + default 配合实现
 */
@Slf4j
public class BeanUtils {

    public static <T> T toBean(Object source, Class<T> targetClass) {
        return BeanUtil.toBean(source, targetClass);
    }

    public static <T> T toBean(Object source, Class<T> targetClass, Consumer<T> peek) {
        T target = toBean(source, targetClass);
        if (target != null) {
            peek.accept(target);
        }
        return target;
    }

    public static <S, T> List<T> toBean(List<S> source, Class<T> targetType) {
        if (source == null) {
            return null;
        }
        return CollectionUtils.convertList(source, s -> toBean(s, targetType));
    }

    public static <S, T> List<T> toBean(List<S> source, Class<T> targetType, Consumer<T> peek) {
        List<T> list = toBean(source, targetType);
        if (list != null) {
            list.forEach(peek);
        }
        return list;
    }

    public static <S, T> Page<T> toBean(Page<S> source, Class<T> targetType) {
        return toBean(source, targetType, null);
    }

    public static <S, T> Page<T> toBean(Page<S> source, Class<T> targetType, Consumer<T> peek) {
        if (source == null) {
            return null;
        }
        List<T> list = toBean(source.getRecords(), targetType);
        if (peek != null) {
            list.forEach(peek);
        }
        Page<T> objectPage = new Page<>();
        objectPage.setRecords(list);
        objectPage.setTotal(source.getTotal());
        return objectPage;
    }

    public static Set<Class<?>> scanClass(String scanPackage, Class<? extends Annotation> annotation){
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        ApplicationContext applicationContext = SpringUtil.getApplicationContext();
        scanner.setEnvironment(applicationContext.getEnvironment());
        scanner.setResourceLoader(applicationContext);
        scanner.addIncludeFilter(new AnnotationTypeFilter(annotation));
        Set<Class<?>> resultSet = new HashSet<>();
        for (BeanDefinition candidate : scanner.findCandidateComponents(scanPackage)) {
            try {
                if (!StringUtils.hasText(candidate.getBeanClassName())){
                    continue;
                }
                resultSet.add(ClassUtils.forName(candidate.getBeanClassName(), applicationContext.getClassLoader()));
            } catch (ClassNotFoundException e) {
                log.warn("扫描类：{}未找到！", e.getMessage());
            }
        }
        return resultSet;
    }
}