package br.com.acerto.client;

public class DuplicatePhoneException extends RuntimeException {
    public DuplicatePhoneException(String phone) {
        super("A client with the given phone already exists: \n" + phone);
    }
}
