package it.polito.tdp.formulaone.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.formulaone.db.FormulaOneDAO;

public class Model {
	
	private List<Season> stagioni;
	FormulaOneDAO dao;
	private SimpleDirectedWeightedGraph<Driver,DefaultWeightedEdge> grafo;
	
	//variabili di stato della ricorsione
	private int tassoMin;
	private List<Driver> teamMin;
	
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
				peso-=grafo.getEdgeWeight(entrante);
			}
			if(peso>max){
				max=peso;
				best=d;
			}
		}
		return best;
	}
	
	public List<Driver> getDreamteam(int k){
		
		Set<Driver> team=new HashSet<Driver>();
		tassoMin=Integer.MAX_VALUE;
		teamMin=null;
		
		recursive(0,team,k);
		
		return teamMin;
	}
	
	/**
	 * IN INGRESSO RICEVO IL TEAM PARZIALE COMPOSTO DI PASSO ELEMENTI
	 * LA VARIABILE PASSO PARTE DA 0
	 * IL CASO TERMINALE è QUANDO PASSO=K ED IN QUEL CASO VA CALCOLATO IL TASSO DI SCONFITTA
	 * ALTRIMENTI SI PROCEDE RICORSIVAMENTE AD AGGIUNGERE UN NUOVO VERTICE (IL PASSO+1-ESIMO),
	 * SCEGLIENDO TRA I VERTICI NON ANCORA PRESENTI NEL TEAM
	 * 
	 * @param passo
	 * @param team
	 * @param k
	 */
	private void recursive(int passo,Set<Driver> team, int k){
		
		if(passo==k){
			//calcolare tasso di sconfitta del team
			int tasso=this.tassoSconfittaTeam(team);
			
			//eventualmente aggiornare il minimo
			if(tasso<tassoMin){
				tassoMin=tasso;
				teamMin=new ArrayList<>(team);//creo un nuova lista partendo da una lista attuale
				System.out.println(tassoMin+" "+team.toString());
			}
			//return; POSSO METTERE UN RETURN INVECE DI UN IF ELSE GENERALMENTE
			//I DUE MODO SONO (FORSE) LOGICAMENTE IDENTICI
			
		}else{
			//caso normale
			Set<Driver> candidati=new HashSet<>(grafo.vertexSet());
			candidati.removeAll(team);
			
			for(Driver d: candidati){
				team.add(d);
				recursive(passo+1,team,k);
				team.remove(d);
			}
		}	
	}

	private int tassoSconfittaTeam(Set<Driver> team) {
		int tasso=0;
		
		for(DefaultWeightedEdge e: grafo.edgeSet()){
			if(!team.contains(grafo.getEdgeSource(e)) && team.contains(grafo.getEdgeTarget(e))){
				tasso+=grafo.getEdgeWeight(e);
			}
		}
		return tasso;
	}
}
