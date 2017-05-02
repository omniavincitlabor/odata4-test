package test;

import com.sdl.odata.controller.AbstractODataController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 */
@Controller
@RequestMapping("/example.svc/**")
public class TestController extends AbstractODataController {
}
