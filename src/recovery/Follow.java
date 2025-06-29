package recovery;

import compilador.*;

public class Follow { //implementa os conjuntos first p/ alguns nao terminais

    static public final RecoverySet main = new RecoverySet();
    static public final RecoverySet comandos = new RecoverySet();
    static public final RecoverySet declaraVariavel = new RecoverySet();
    static public final RecoverySet declaraVariavelFor = new RecoverySet();
    static public final RecoverySet tipoVariavel = new RecoverySet();
    static public final RecoverySet operadorMatematico = new RecoverySet();
    static public final RecoverySet operadorLogico = new RecoverySet();
    static public final RecoverySet expressao = new RecoverySet();
    static public final RecoverySet expressaoLogica = new RecoverySet();
    static public final RecoverySet termo = new RecoverySet();
    static public final RecoverySet atribuiValor = new RecoverySet();
    static public final RecoverySet sepaIf = new RecoverySet();
    static public final RecoverySet aconteceWhile = new RecoverySet();
    static public final RecoverySet vainafeFor = new RecoverySet();
    static public final RecoverySet incDec = new RecoverySet();

    static {
        // follow do main
        main.add(new Integer(CMaisMenosConstants.EOF));

        // follow do comandos
        comandos.add(new Integer(CMaisMenosConstants.FECHABLOCO));

        // follow do declaraVariavel
        declaraVariavel.add(new Integer(CMaisMenosConstants.TIPOINTEIRO));
        declaraVariavel.add(new Integer(CMaisMenosConstants.TIPOFLOAT));
        declaraVariavel.add(new Integer(CMaisMenosConstants.TIPOBOOL));
        declaraVariavel.add(new Integer(CMaisMenosConstants.ID));
        declaraVariavel.add(new Integer(CMaisMenosConstants.SEPA));
        declaraVariavel.add(new Integer(CMaisMenosConstants.ACONTECE));
        declaraVariavel.add(new Integer(CMaisMenosConstants.VAINAFE));
        declaraVariavel.add(new Integer(CMaisMenosConstants.FECHABLOCO));

        // follow do declaraVariavelFor
        declaraVariavelFor.add(new Integer(CMaisMenosConstants.PONTOVIRGULA));

        // follow do tipoVariavel
        tipoVariavel.add(new Integer(CMaisMenosConstants.ID));

        // follow do operadorMatematico
        operadorMatematico.add(new Integer(CMaisMenosConstants.ID));
        operadorMatematico.add(new Integer(CMaisMenosConstants.DECIMAL));

        // follow do operadorLogico
        operadorLogico.add(new Integer(CMaisMenosConstants.ID));
        operadorLogico.add(new Integer(CMaisMenosConstants.DECIMAL));

        // follow da expressao
        expressao.add(new Integer(CMaisMenosConstants.VIRGULA));
        expressao.add(new Integer(CMaisMenosConstants.PONTOVIRGULA));
        expressao.add(new Integer(CMaisMenosConstants.FECHAPAR));
        expressao.add(new Integer(CMaisMenosConstants.IGUAL_LOG));
        expressao.add(new Integer(CMaisMenosConstants.DIFERENTE));
        expressao.add(new Integer(CMaisMenosConstants.MENOR_IGUAL));
        expressao.add(new Integer(CMaisMenosConstants.MAIOR_IGUAL));
        expressao.add(new Integer(CMaisMenosConstants.MENOR));
        expressao.add(new Integer(CMaisMenosConstants.MAIOR));


        // follow da expressaoLogica
        expressaoLogica.add(new Integer(CMaisMenosConstants.FECHAPAR));
        expressaoLogica.add(new Integer(CMaisMenosConstants.PONTOVIRGULA));

        // follow do termo
        termo.add(new Integer(CMaisMenosConstants.MAIS));
        termo.add(new Integer(CMaisMenosConstants.MENOS));
        termo.add(new Integer(CMaisMenosConstants.MULTI));
        termo.add(new Integer(CMaisMenosConstants.DIVISAO));
        termo.add(new Integer(CMaisMenosConstants.VIRGULA));
        termo.add(new Integer(CMaisMenosConstants.PONTOVIRGULA));
        termo.add(new Integer(CMaisMenosConstants.FECHAPAR));

        // follow do atribuiValor
        atribuiValor.add(new Integer(CMaisMenosConstants.TIPOINTEIRO));
        atribuiValor.add(new Integer(CMaisMenosConstants.TIPOFLOAT));
        atribuiValor.add(new Integer(CMaisMenosConstants.TIPOBOOL));
        atribuiValor.add(new Integer(CMaisMenosConstants.ID));
        atribuiValor.add(new Integer(CMaisMenosConstants.SEPA));
        atribuiValor.add(new Integer(CMaisMenosConstants.ACONTECE));
        atribuiValor.add(new Integer(CMaisMenosConstants.VAINAFE));
        atribuiValor.add(new Integer(CMaisMenosConstants.FECHABLOCO));
        
        // follow do sepaIf
        sepaIf.add(new Integer(CMaisMenosConstants.TIPOINTEIRO));
        sepaIf.add(new Integer(CMaisMenosConstants.TIPOFLOAT));
        sepaIf.add(new Integer(CMaisMenosConstants.TIPOBOOL));
        sepaIf.add(new Integer(CMaisMenosConstants.ID));
        sepaIf.add(new Integer(CMaisMenosConstants.SEPA));
        sepaIf.add(new Integer(CMaisMenosConstants.ACONTECE));
        sepaIf.add(new Integer(CMaisMenosConstants.VAINAFE));
        sepaIf.add(new Integer(CMaisMenosConstants.FECHABLOCO));

        // follow do aconteceWhile
        aconteceWhile.add(new Integer(CMaisMenosConstants.TIPOINTEIRO));
        aconteceWhile.add(new Integer(CMaisMenosConstants.TIPOFLOAT));
        aconteceWhile.add(new Integer(CMaisMenosConstants.TIPOBOOL));
        aconteceWhile.add(new Integer(CMaisMenosConstants.ID));
        aconteceWhile.add(new Integer(CMaisMenosConstants.SEPA));
        aconteceWhile.add(new Integer(CMaisMenosConstants.ACONTECE));
        aconteceWhile.add(new Integer(CMaisMenosConstants.VAINAFE));
        aconteceWhile.add(new Integer(CMaisMenosConstants.FECHABLOCO));
        
        // follow do vainafeFor
        vainafeFor.add(new Integer(CMaisMenosConstants.TIPOINTEIRO));
        vainafeFor.add(new Integer(CMaisMenosConstants.TIPOFLOAT));
        vainafeFor.add(new Integer(CMaisMenosConstants.TIPOBOOL));
        vainafeFor.add(new Integer(CMaisMenosConstants.ID));
        vainafeFor.add(new Integer(CMaisMenosConstants.SEPA));
        vainafeFor.add(new Integer(CMaisMenosConstants.ACONTECE));
        vainafeFor.add(new Integer(CMaisMenosConstants.VAINAFE));
        vainafeFor.add(new Integer(CMaisMenosConstants.FECHABLOCO));
        
        // follow do incremento ou decremento
        incDec.add(new Integer(CMaisMenosConstants.FECHAPAR));
        

    }
}
