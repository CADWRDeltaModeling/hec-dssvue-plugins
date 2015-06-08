package hecdssvue.cdec.plugin;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class TestImageIO {
	public static void main(String[] args) throws IOException{
		Class thisClass = TestImageIO.class;
		URL resource = thisClass.getResource("resources/standard_waypoint.png");
		BufferedImage img = ImageIO.read(resource);
	}
}
