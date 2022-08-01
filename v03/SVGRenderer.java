package v03;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

/**
 * This class is to create a svg file storing the snapshot of the graph 
 * 
 * @author piersaj
 *
 */
public class SVGRenderer {
	private String fileName = null;
	private PrintWriter writer = null;
	
	
	private int fillR=0, fillG=0, fillB=0;
	private int strokeR=0, strokeG=0, strokeB=0;

	
	private boolean useTimestamp = true;

	public SVGRenderer(String filename, int width, int heigh, boolean useTimestamp){
		this (filename, width, heigh, 0, 0, useTimestamp);
	}
	
	public SVGRenderer(String filename, int width, int heigh){
		this (filename, width, heigh, 0, 0, false);
	}
	
	public SVGRenderer(String filename){
		this (filename, 100, 100);
	}

	
	public SVGRenderer(String filename, int width, int heigh, int viewportx, int viewporty, boolean useTimestamp){

		this.fileName = filename;
		this.useTimestamp = useTimestamp;
		
		if (this.useTimestamp){
			/*
			 * remove file extension from name "file.svg" >> "file"
			 */
			int i = filename.lastIndexOf(".");
			if (i != -1){
				this.fileName = filename.substring(0, i);
			}
			
			/*
			 * add timestamp:
			 * "file" >> file.rrrr-mm-dd-hh-mm-ss.svg
			 */
			this.fileName = String.format("%s_%04d-%02d-%02d-%02d-%02d-%02d",
					this.fileName,
					Calendar.getInstance().get(Calendar.YEAR),
					Calendar.getInstance().get(Calendar.MONDAY),
					Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
					Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
					Calendar.getInstance().get(Calendar.MINUTE),
					Calendar.getInstance().get(Calendar.SECOND)
			);
		}	// if
		
		if (!filename.endsWith(".svg")){
			this.fileName += ".svg";
		}
		
		
		
		try {
			writer = new PrintWriter(this.fileName);
		} catch (IOException ex){
			System.err.println(ex);
			ex.printStackTrace();
			writer = null;
		} finally {
			// nothing here
		}
		
		writeHeader(width, heigh, viewportx, viewporty);
	}	// this()

	
	private void writeHeader(int w, int h){
		writer.write("<?xml version=\"1.0\" standalone=\"no\"?>\n"
				+ "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\"\n"
				+ "\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n"
				+ "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\"\n"
				+ "width=\""+ w +"px\" height=\""+ h +"px\" >\n"
				
		);
		writer.flush();	
	}	// writeHeader();
	
	
	private void writeHeader(int w, int h, int minx, int miny){
		writer.write("<?xml version=\"1.0\" standalone=\"no\"?>\n"
				+ "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\"\n"
				+ "\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n"
				+ "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\"\n"
				+ "width=\""+ w +"px\" height=\""+ h +"px\" \n"
				+ "viewbox =\"" +minx+" "+ miny +" "+ (minx+w)+" "+ (miny+h) +"\">\n" 
				
		);
		writer.flush();
		
	}	// writeHeader();
	
	

	public void drawRect(int width, int height, int x , int y){
		if (writer == null) {
			return;
		}
		writer.format("<rect width=\"%d\" height=\"%d\" x=\"%d\" y=\"%d\" ", width, height, x, y);
		printFillStroke();
		writer.format("/>\n"); 
	}
	
	/*
	 *  TODO implement this ...
	 * 
	 */
	public void printAxes(){
		
	}
	
	private void printFillStroke(){
		writer.format("fill=\"rgb(%d,%d,%d)\" stroke-width=\"1\" stroke=\"rgb(%d,%d,%d)\"", fillR, fillG, fillB, strokeR, strokeG, strokeB);
	}
	
	public void drawCircle(int radius, int x , int y){
		if (writer == null) {
			return;
		}
		writer.format("<ellipse rx=\"%d\" ry=\"%d\" cx=\"%d\" cy=\"%d\" ", radius, radius, x, y);
		printFillStroke();
		writer.format("/>\n"); 
	}

	public void drawLine(int startX, int statyY, int destX, int destY, int strokeWidth){
		writer.format("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\"", startX, statyY, destX, destY);
		writer.format(" stroke=\"rgb(%d,%d,%d)\" stroke-width=\"%d\" />\n", strokeR, strokeG, strokeB, strokeWidth);
	}
	
	public void drawLine(int startX, int statyY, int destX, int destY){
		drawLine(startX, statyY, destX, destY, 1);
	}
	
	
	public void printText(int x, int y, String text, int size){
		writer.format("<text x=\"%d\" y=\"%d\" font-family=\"%s\" font-size=\"%d\" fill=\"%s\" >\n",
				x, y, "Verdana", size, "black");
		writer.format("\t%s\n</text>\n", text);
	}
	
	public void printText(int x, int y, String text){
		printText(x, y, text, 20);
	}
		
	public void setFillRGB(int r, int g, int b){
		if (r>=0 && r<=255)
			fillR = r;
		if (g>=0 && g<=255)
			fillG = g;
		if (b>=0 && b<=255)
			fillB = b;
	}	// setFillRGB
	
	public void setStrokeRGB(int r, int g, int b){
		if (r>=0 && r<=255)
			strokeR = r;
		if (g>=0 && g<=255)
			strokeG = g;
		if (b>=0 && b<=255)
			strokeB = b;
	}	// setStrokeRGB

	
	public void closeSnapshot(){
		writeFooter();
		writer.flush();
		writer.close();
	}
	
	private void writeFooter(){
		writer.write("</svg>\n"	);
	}	// writeHeader();
	

	
	

	
	public static void main(String[] args) {
		SVGRenderer s = new SVGRenderer("/home/piersaj/Desktop/image.svg", 200, 200, true);
		s.drawRect(213, 143, 65, 35);
		s.drawCircle( 143, 265, 135);
		s.setStrokeRGB(255, 0, 0);
		
		s.drawLine(3, 45, 70, 310);
		s.closeSnapshot();
	}	// main
	
	
}	// class
