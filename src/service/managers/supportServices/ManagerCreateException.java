package service.managers.supportServices;

public class ManagerCreateException extends RuntimeException {
    public ManagerCreateException(){}
    public ManagerCreateException(String message){
        super(message);
    }
}
