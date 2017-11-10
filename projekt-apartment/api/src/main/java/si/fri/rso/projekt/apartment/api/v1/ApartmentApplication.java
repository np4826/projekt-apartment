package si.fri.rso.projekt.apartment.api.v1;

import com.kumuluz.ee.discovery.annotations.RegisterService;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/v1")
@RegisterService
public class ApartmentApplication extends Application {

}
