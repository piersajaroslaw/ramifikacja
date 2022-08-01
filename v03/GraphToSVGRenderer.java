package v03;

import v01.Graf;
import v01.Krawedz;
import v01.Punkt;

/**
 * 
 * 
 */
public class GraphToSVGRenderer {

	private Graf g = null;
	private SVGRenderer r = null;
	private boolean useTimestamp = true;
	
	private int offsetx = 45;
	private int offsety = 20;
	
	public GraphToSVGRenderer(Graf g){
		this.g = g;
	
		
		r = new SVGRenderer("/home/piersaj/Desktop/ramifikacja", (int) Punkt.MAX_X + offsetx
				,  (int) Punkt.MAX_Y + offsety,  useTimestamp);
	}	//
	
	
	public void renderGraphSnapshot(){
		
		renderEdges();
		renderNodes();
		renderAxes();
		r.closeSnapshot();
	}
	
	
	private void renderEdges(){
		int w = 0;;
		for (int i=0; i<g.getIloscWierzcholkow(); i++){
			Punkt q = g.getWierzcholek(i);
			for (Krawedz k: g.getSasiedzi(i)){

				setEdgeColor(k);
				Punkt p = g.getWierzcholek( g.indeks(k.getB()) );
				
				w = (int) Math.sqrt(k.getPrzeplyw());
				w = w<=0 ? 1 : w;
				
				r.drawLine(offsetx + (int)q.getX(), (int)q.getY(), 
						offsetx + (int)p.getX(), (int)p.getY(), w );
			}	// for j
		}	// for i
		
	}	// renderGraph
	
	private void renderAxes(){
		r.setFillRGB(0, 0, 0);
		r.setStrokeRGB(0, 0, 0);
		
		int maxY = (int) Punkt.MAX_Y;
		
		r.drawLine(offsetx + 0, maxY - 0, offsetx + 0,  0);
		r.drawLine(offsetx + 0, maxY - 0, offsetx + (int) Punkt.MAX_X,  maxY - 0);
		
		int tickLen= 5;
		int tickGap = 100;
		
		// OX axis
		for (int x= tickGap; x <= Punkt.MAX_X; x+= tickGap){
			r.drawLine(offsetx + x, maxY - tickLen, offsetx + x, maxY + tickLen);
			r.printText(offsetx + x - offsetx/2 , offsety + maxY , ""+x);
		}
		
		// OY axis
		for (int y= tickGap; y <= Punkt.MAX_Y; y+= tickGap){
			r.drawLine(offsetx + tickLen, maxY - y ,offsetx -tickLen, maxY - y);
			r.printText(1 , maxY - y, ""+y); // zero offset here!
		}
		
	}	// renderAxes
	
	private void renderNodes(){
		int radius = 2;
		for (Punkt p : g.getWierzcholki()){
			if (p.getTyp()==Punkt.TYP_INNY){
				setNodeColor(p);
				r.drawCircle(radius, offsetx + (int)p.getX(), (int)p.getY());
			}
		}
		
		for (Punkt p : g.getWierzcholki()){
			if (p.getTyp() == Punkt.TYP_ODBIORCA || p.getTyp() == Punkt.TYP_ZRODLO){
				setNodeColor(p);
				r.drawCircle(radius, offsetx + (int)p.getX(), (int)p.getY());
			}	// fi
		}
	}	// render Nodes
	
	
	
	private void setEdgeColor(Krawedz k){
		// some rough scaling
		double min  = 0;
		double max = 10;
		double col = k.getPrzeplyw() / (max-min);
		col = col < 0 ? 0 : col;
		col = col > 1? 1 : col;
		
		/* scalars
		 * 
		 */
		double scalr = 0;
		double scalg = 0;
		double scalb = 255;
		
		r.setStrokeRGB( (int)(scalr * col) , (int)(scalg * col), (int)(scalb * col));
		
		
		
	}	// setEdgeColor
	
	private void setNodeColor(Punkt p){
		switch (p.getTyp()) {
		case Punkt.TYP_ODBIORCA:
			setDestColors();
			break;
		case Punkt.TYP_ZRODLO:
			setSourceColors();
			break;
			
		case Punkt.TYP_INNY: // jump to default	
		default:
			setTransColors();
			break;
		}
	}
	
	
	private void setSourceColors(){
		r.setFillRGB(0, 255, 0);
		r.setStrokeRGB(0, 230, 0);
	}
	
	private void setDestColors(){
		r.setFillRGB(255, 0, 0);
		r.setStrokeRGB(230, 0, 0);
	}
	
	private void setTransColors(){
		r.setFillRGB(0, 0, 255);
		r.setStrokeRGB(0, 0, 240);
	}

	
}	// class
