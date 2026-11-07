package model;

/** Objeto que avisa seus observadores quando muda (padrão Observer, Cap. 17). */
public interface ObservadoIF {

	/** Registra o observador; registrar o mesmo duas vezes não faz efeito. */
	void add(ObservadorIF o);

	/** Cancela o registro do observador. */
	void remove(ObservadorIF o);

	/** Dado do observado pedido pelo observador depois do aviso; cada observado define os códigos. */
	int get(int i);

}