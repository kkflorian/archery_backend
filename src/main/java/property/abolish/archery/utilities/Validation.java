package property.abolish.archery.utilities;

import io.javalin.core.validation.BodyValidator;
import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import property.abolish.archery.http.model.responses.ErrorResponse;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Validation {

    public static boolean isNullOrEmpty(String field) {
        return field == null || field.isEmpty();
    }

    public static void handleValidationError(Context ctx, Validator<?> validator) {
        Map<String, List<String>> errors = validator.errors();
        String error = errors.values().iterator().next().get(0);
        ctx.status(400).json(new ErrorResponse("VALIDATION_ERROR", error));
    }

    public static <T> boolean checkList(String listName, List<T> list, Consumer<BodyValidator<T>> validatorConsumer) {
        if (list == null) return false;
        for (int index = 0; index < list.size(); index++) {
            final BodyValidator<T> validator = new BodyValidator<>(list.get(index),
                    listName + " list entry at index " + index);
            validatorConsumer.accept(validator);
            validator.get();
        }
        return true;
    }
}
