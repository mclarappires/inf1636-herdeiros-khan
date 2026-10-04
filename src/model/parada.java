package model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


class parada {
	
	private final int id;
	private final int capacidadePeoes;
	private final int capacidadeYurts;
	
	private final Map<Integer, parada> adjacentes = new HashMap<Integer, parada>();
	
	private final List<Yurt> yurts = new LinkedList<Yurt>();
	
	
}
