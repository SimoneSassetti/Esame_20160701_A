package it.polito.tdp.formulaone.model;

import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.formulaone.db.FormulaOneDAO;

public class Model {
	
	private List<Season> stagioni;
	FormulaOneDAO dao;
	private SimpleDirectedWeightedGraph<Driver,DefaultWeightedEdge> grafo;
	
	public Model(){
		dao=new FormulaOneDAO();
	}
	
	public List<Season> getStagioni(){
		if(stagioni==null){
			stagioni=dao.getAllSeasons();
		}
		return stagioni;
	}
	
	public void creaGrafo(Season s){
		
		grafo=new SimpleDirectedWeightedGraph<Driver,DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		List<Driver> drivers=dao.getDriverForSeason(s);
		
		Graphs.addAllVertices(grafo, drivers);
		
		for(Driver d1:grafo.vertexSet()){
			for(Driver d2: grafo.vertexSet()){
				if(!d2.equals(d1)){
					int vittorie=dao.contaVittorie(d1, d2, s);
					if(vittorie>0){
						Graphs.addEdgeWithVertices(grafo, d1, d2, vittorie);
					}
				}
			}
		}
	}

	public Driver getMigliore(){
		Driver best=null;
		int max=Integer.MIN_VALUE;
		
		for(Driver d: grafo.vertexSet()){
			
			int peso=0;
			
			for(DefaultWeightedEdge uscente: grafo.outgoingEdgesOf(d)){
				peso+=grafo.getEdgeWeight(uscente);
			}
			for(DefaultWeightedEdge entrante: grafo.incomingEdgesOf(d)){
				peso+=grafo.getEdgeWeight(entrante);
			}
			if(peso>max){
				max=peso;
				best=d;
			}
		}
		return best;
	}
}
