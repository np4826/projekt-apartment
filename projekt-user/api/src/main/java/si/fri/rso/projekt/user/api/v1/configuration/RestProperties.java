package si.fri.rso.projekt.user.api.v1.configuration;
import com.kumuluz.ee.configuration.cdi.ConfigBundle;
import com.kumuluz.ee.configuration.cdi.ConfigValue;

import javax.enterprise.context.ApplicationScoped;

@ConfigBundle("rest-properties")
@ApplicationScoped
public class RestProperties {

    @ConfigValue(watch = true)
    private boolean healthy;
    // docker exec etcd etcdctl --endpoints //192.168.99.100:2379 set /environments/dev/services/rso-user/1.0.0/config/rest-properties/healthy true

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }
}