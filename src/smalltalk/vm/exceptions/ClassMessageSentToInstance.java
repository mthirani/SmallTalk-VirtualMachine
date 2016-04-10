package smalltalk.vm.exceptions;

/**
 * Created by Mayank on 4/5/2016.
 */
public class ClassMessageSentToInstance extends VMException {
    public ClassMessageSentToInstance(String message, String vmStackTrace) {
        super(message, vmStackTrace);
    }
}
