package dev.george.biolink.exception;

public class ProfileNotFoundException extends Throwable {

    public ProfileNotFoundException() {
        super("Unable to find a profile with that data!");
    }
}
