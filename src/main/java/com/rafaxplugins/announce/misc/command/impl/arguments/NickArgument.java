package com.rafaxplugins.announce.misc.command.impl.arguments;

public class NickArgument extends Argument {


    public NickArgument(String name, String description, boolean required) {
        super(name, description, required);
    }

    public NickArgument(String name, String description) {
        super(name, description);
    }

    @Override
    public boolean isValid(String arg) {
        return ("[a-zA-Z0-9_]{3,16}").matches(arg);
    }

    @Override
    public String getErrorMessage(String arg) {
        return "Jogador inválido.";
    }
}
