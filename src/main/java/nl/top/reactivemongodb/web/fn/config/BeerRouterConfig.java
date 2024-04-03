package nl.top.reactivemongodb.web.fn.config;

import nl.top.reactivemongodb.web.fn.handlers.BeerHandler;
import nl.top.reactivemongodb.web.fn.handlers.ResourceHandler;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeerRouterConfig extends GenericRouterConfig<ResourceHandler<BeerHandler>> {

    public static final String BEER_PATH = "/api/v3/beer";
    public static final String BEER_PATH_ID = BEER_PATH + "/{beerId}";

    public BeerRouterConfig(BeerHandler handler) {
        super(BEER_PATH, BEER_PATH_ID, handler);
    }

}
