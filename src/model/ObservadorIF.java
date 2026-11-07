package model;

/**
 * Quem quer ser avisado quando um objeto observado muda (padrão Observer, Cap. 17).
 * Ao receber o aviso, o observador pergunta ao observado o que precisa com {@code o.get(...)}.
 */
public interface ObservadorIF {

	/** Chamado pelo observado sempre que o seu estado muda. */
	void notify(ObservadoIF o);

}
 