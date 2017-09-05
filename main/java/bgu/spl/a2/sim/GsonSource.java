package bgu.spl.a2.sim;

/**
 * Created by snir on 30/12/16.
 */
public class GsonSource {
    int threads;
    GPlans[] plans;
    GTools[] tools;
    GProduct[][] waves;

    static class GPlans {
        String product;
        String[] tools;
        String[] parts;
    }

    static class GTools {
        String tool;
        int qty;
    }

    static class GProduct {
        String product;
        int qty;
        long startId;

    }


}
