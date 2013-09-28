package com.ltst.przwrd.tools;

import org.omich.velo.constants.Strings;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 06.09.13.
 */
public class DeclensionTools {

    /**
     * Возвращает единицу измерения с правильным окончанием
     *
     * @param {Number} num      Число
     * @param {Object} cases    Варианты слова {nom: 'час', gen: 'часа', plu: 'часов'}
     * @return {String}
     */
    static public @Nonnull String units(int num, @Nonnull String[] cases) {

        if(cases.length == 0) return null;

        num = Math.abs(num);
        @Nonnull String word = Strings.EMPTY;

        if (String.valueOf(num).indexOf(".") > -1)
        {
            word = cases[1];
        }
        else
        {
            word = (
                    num % 10 == 1 && num % 100 != 11
                            ? cases[0]
                            : num % 10 >= 2 && num % 10 <= 4 && (num % 100 < 10 || num % 100 >= 20)
                            ? cases[1]
                            : cases[2]
            );
        }
        return word;
    }
}
