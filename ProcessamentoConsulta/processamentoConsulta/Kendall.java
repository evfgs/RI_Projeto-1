package processamentoConsulta;

public class Kendall {
	
	private static int arrayX[] = {1,2,3,4,5};
	private static int arrayY[] = {2,3,1,5,4};
	
	public static void main(String[] args) {
		System.out.println(kendall(arrayX, arrayY));

	}
	
	public static double kendall(int[] x, int[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("Input vector sizes are different.");
        }
        // é preciso saber se os rankings crescem ou diminuem de maneira igual, isso é a concordancia
        // isso é feito subtraindo determinadas posições dos rankings(pares) e multiplicando os resultados entre os dois rankings
        // se for positivo, quer dizer que essas posições são concordantes(cresceram ou diminuiram ao msm tempo)
        // se for negativo, quer dizer que são descordantes(um cresceu enquanto o outro diminuiu)
        // se for negativo, quer dizer que teve algum empate dentro de um dos rankings
        double is = 0, n = x.length;
        //is = concordantes - descordantes
        // n1 e n2 = pares validos(sem empate) para cada ranking
        double aa, a2, a1;
        for (int j = 0; j < n - 1; j++) {
            for (int k = j + 1; k < n; k++) {
                a1 = x[j] - x[k];
                a2 = y[j] - y[k];
                aa = a1 * a2;
                if (aa != 0.0) {
                    if (aa <= 0) {
                        is = is +2;
                    }

                }
            }
        }
        // (C-D)/(C+D)
        // a separação do n1 e n2 é apenas para eliminar os casos em que ou a1 ou a2 seja zero
        double tau = 1.0 - (2 * is)/(n*(n-1));
        //double tau = is / (Math.sqrt(n1 * n2));
        return tau;
    }

}
