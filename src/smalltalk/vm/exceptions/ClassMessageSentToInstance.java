package smalltalk.vm.exceptions;

public class ClassMessageSentToInstance extends VMException {
    public ClassMessageSentToInstance(String message, String vmStackTrace) {
        super(message, vmStackTrace);
    }
}
