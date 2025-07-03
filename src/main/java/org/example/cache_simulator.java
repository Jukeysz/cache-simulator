package org.example;

import java.io.DataInputStream;
import java.io.FileInputStream;

public class cache_simulator {
    public static void main(String[] args) {
        if (args.length != 6){
            //System.out.println(args.length);
            System.out.println("Numero de argumentos incorreto. Utilize:");
            System.out.println("java cache_simulator <nsets> <bsize> <assoc> <substituição> <flag_saida> arquivo_de_entrada");
            System.exit(1);
        }
        int nsets = Integer.parseInt(args[0]);
        int bsize = Integer.parseInt(args[1]);
        int assoc = Integer.parseInt(args[2]);

        int []cache_val = new int[nsets * assoc];
        int []cache_tag = new int[nsets * assoc];

        int n_bits_offset = (int) (Math.log(nsets) / Math.log(2));
        int n_bits_indice = (int) (Math.log(nsets) / Math.log(2));

        int n_bites_tag = 32 - n_bits_offset - n_bits_indice;;

        String subst = args[3];
        int flagOut = Integer.parseInt(args[4]);
        String arquivoEntrada = args[5];

        System.out.printf("nsets = %d\n", nsets);
        System.out.printf("bsize = %d\n", bsize);
        System.out.printf("assoc = %d\n", assoc);
        System.out.printf("subst = %s\n", subst);
        System.out.printf("flagOut = %d\n", flagOut);
        System.out.printf("arquivo = %s\n", arquivoEntrada);

        // Seu codigo vai aqui

        int hits = 0, misses = 0;
        int time = 0;

        try (DataInputStream dis = new DataInputStream(new FileInputStream(arquivoEntrada))) {
            while (dis.available() > 0) {
                int address = dis.readInt();

                int offset = address & ((1 << n_bits_offset) - 1);

            }
        } catch ();
    }
}