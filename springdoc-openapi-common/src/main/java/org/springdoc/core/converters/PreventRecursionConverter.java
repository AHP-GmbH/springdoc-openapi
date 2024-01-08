package org.springdoc.core.converters;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.ArrayType;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.reactivestreams.Publisher;
import org.springdoc.core.providers.ObjectMapperProvider;
import reactor.core.publisher.Flux;

import java.util.Iterator;
import java.util.Stack;

import static org.springdoc.core.SpringDocUtils.getConfig;
import static org.springdoc.core.converters.ConverterUtils.isFluxTypeWrapper;
import static org.springdoc.core.converters.ConverterUtils.isResponseTypeWrapper;

/**
 * Prevent Stack Overflow by tracking with stack and map.
 */
public class PreventRecursionConverter implements ModelConverter {

    private final ObjectMapperProvider objectMapperProvider;

    private java.util.Map<String, Schema> schemaMap = new java.util.HashMap<>();
    private java.util.Stack<String> typeStack = new Stack<>();

    /**
     * Instantiates a new PreventRecursionConverter
     *
     * @param objectMapperProvider the object mapper provider
     */
    public PreventRecursionConverter(ObjectMapperProvider objectMapperProvider) {
        this.objectMapperProvider = objectMapperProvider;
    }

    @Override
    public Schema resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        if (isAhpType(type)) {
            if (type.isResolveAsRef() && type.isSchemaProperty()) {
                return getTypeRef(type);
            }
            Schema rc = schemaMap.get(type.getType().getTypeName());
            if (rc != null) {
                return rc;
            }
            typeStack.push(type.getType().getTypeName());
            if (isTodoAsRef(type)) {
                typeStack.pop();
                return getTypeRef(type);
            }
            rc = chain.hasNext() ? chain.next().resolve(type, context, chain) : null;
            typeStack.pop();
            if (rc != null) {
                schemaMap.put(type.getType().getTypeName(), rc);
            }
            return rc;
        } else {
            return chain.hasNext() ? chain.next().resolve(type, context, chain) : null;
        }
    }

    private static final String AHP_START = "[simple type, class de.ahp.iqbasis.dal.models.";

    private boolean isAhpType(AnnotatedType type) {
        return type.getType().getTypeName().startsWith(AHP_START);
    }

    private boolean isTodoAsRef(AnnotatedType type) {
        if (isAhpType(type) && typeStack.size() > 1 &&
                typeStack.peek().equals(type.getType().getTypeName())) {
            for (int i = typeStack.size() - 2; i >= 0; i--) {
                if (typeStack.get(i).equals(type.getType().getTypeName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private Schema getTypeRef(AnnotatedType type) {
        String n = type.getType().getTypeName().substring(AHP_START.length());
        String[] segs = n.split("\\.");
        String lastseg = segs[segs.length - 1];
        lastseg = lastseg.split("\\]")[0];
        String ptype = type.getParent() == null ? "(unknown)" : type.getParent().getName();
        String pname = type.getPropertyName() == null ? "(unknown)" : type.getPropertyName();
        Schema s = new Schema<Object>().$ref("#/components/schemas/" + lastseg)
                .description(ptype + "." + pname + ":" + lastseg);
        return s;
    }
}

