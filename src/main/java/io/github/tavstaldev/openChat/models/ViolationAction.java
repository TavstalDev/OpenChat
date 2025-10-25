package io.github.tavstaldev.openChat.models;

import io.github.tavstaldev.openChat.OpenChat;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ViolationAction {
    private final String operator;
    private final int amount;
    private final String command;

    public ViolationAction(String operator, int amount, String command) {
        this.operator = operator.toLowerCase();
        this.amount = amount;
        this.command = command;
    }

    public String getOperator() {
        return operator;
    }

    public EOperator getOperatorEnum() {
        return switch (this.operator) {
            case "==", "=", "eq", "equals" -> EOperator.EQUALS;
            case "!=", "<>", "ne", "not_equals" -> EOperator.NOT_EQUALS;
            case ">", "gt", "greater_than" -> EOperator.GREATER_THAN;
            case "<", "lt", "less_than" -> EOperator.LESS_THAN;
            case ">=", "gte", "greater_than_or_equal" -> EOperator.GREATER_THAN_OR_EQUAL;
            case "<=", "lte", "less_than_or_equal" -> EOperator.LESS_THAN_OR_EQUAL;
            default -> {
                OpenChat.logger().warn("Unknown operator: " + getOperator() + ", defaulting to EQUALS");
                yield EOperator.EQUALS;
            }
        };
    }

    public int getAmount() {
        return amount;
    }

    public String getCommand() {
        return command;
    }

    public boolean shouldExecute(int violations) {
        return switch (getOperatorEnum()) {
            case EQUALS -> violations == amount;
            case NOT_EQUALS -> violations != amount;
            case GREATER_THAN -> violations > amount;
            case LESS_THAN -> violations < amount;
            case GREATER_THAN_OR_EQUAL -> violations >= amount;
            case LESS_THAN_OR_EQUAL -> violations <= amount;
            default -> false;
        };
    }

    public static @Nullable ViolationAction fromMap(Map<?, ?> map) {
        if (map == null) {
            return null;
        }

        try {
            String operator = (String) map.get("operator");
            Integer amount = (Integer) map.get("amount");
            String command = (String) map.get("command");
            if (operator == null || amount == null || command == null) {
                return null;
            }
            return new ViolationAction(operator, amount, command);
        }
        catch (Exception ex) {
            OpenChat.logger().error("Error parsing ViolationAction from map: \n" + ex.getMessage());
            return null;
        }
    }
}
