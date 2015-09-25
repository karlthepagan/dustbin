package karl.codes.jackson;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by karl on 9/20/15.
 */
public class EnumTypeIdResolver implements TypeIdResolver {
    private JavaType baseType;
    private Class<? extends Enum> baseEnum;
    private Map<Class<?>,Enum<?>> idFromClass = new HashMap<>();

    @Override
    public void init(JavaType baseType) {
        this.baseType = baseType;

        Class<?> rawClass = baseType.getRawClass();
        JsonTypeInfo typeInfo = rawClass.getAnnotation(JsonTypeInfo.class);

        try {
            // TODO use annotations to discover field name when it doesn't match property name
            baseEnum = (Class<? extends Enum>)rawClass.getField(typeInfo.property()).getType();
            assert Enum.class.isAssignableFrom(baseEnum);
            assert JsonTypeEnum.class.isAssignableFrom(baseEnum);

            for(Object e : baseEnum.getEnumConstants()) {
                idFromClass.put(((JsonTypeEnum) e).resolveType(), (Enum<?>)e);
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String idFromValue(Object value) {
        return idFromValueAndType(value, value.getClass());
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        return idFromClass.get(suggestedType).name();
    }

    @Override
    public String idFromBaseType() {
        return "";
    }

    @Override
    public JavaType typeFromId(String id) {
        Class<?> resolved = ((JsonTypeEnum)Enum.valueOf(baseEnum,id)).resolveType();
        return TypeFactory.defaultInstance().constructSpecializedType(baseType,resolved);
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) {
        return typeFromId(id);
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.NAME;
    }
}
