package org.chocolatemilk.decoder;

import java.util.HashMap;
public class ICTSlatDecoder
{
    private final String[] scientist_names =
            {
                    "shannon",
                    "plato",
                    "schottky",
                    "nyquist",
                    "hamming",
                    "turing",
                    "fourier",
                    "wiener",
                    "gallager",
                    "knuth",
                    "mackay",
                    "bell",
                    "kolmogorov",
                    "gauss",
                    "zuse",
                    "marconi",
                    "bernoulli",
                    "ohm",
                    "kirchhoff"
            };
    public ICTSlatDecoder()
    {

    }

    public String Decode(String input)
    {
        String output = "Bad input. (no scientists found)";
        for(int offset = 0; offset < 3; ++offset)
        {
            String input_with_offset = input.substring(offset);
            ICTSlatToBitstream converter = new ICTSlatToBitstream(input_with_offset);
            String binary_string = converter.Convert();
            ICTBitstringToPlaintext decoder = new ICTBitstringToPlaintext(binary_string);
            String tmp = decoder.getString();
            for(int i = 0; i < scientist_names.length; ++i)
            {
                if(tmp.contains(scientist_names[i]))
                {
                    output = tmp;
                    break;
                }
            }
            if(output != "Bad input. (no scientists found)")
            {
                break;
            }
        }
        return output;
    }

    /*public static void main(String[] args)
    {
	    String input = System.console().readLine();
	    ICTSlatDecoder decoder = new ICTSlatDecoder();
        System.out.println(decoder.Decode(input));
    }*/


    private class ICTBitstringToPlaintext
    {
        private final HashMap<String, Character> huffman_dict = new HashMap<String, Character>()
        {{
                put("000",        ' ');
                put("0111",       'a');
                put("101110",     'b');
                put("11110",      'c');
                put("00110",      'd');
                put("110",        'e');
                put("10000",      'f');
                put("010100",     'g');
                put("11111",      'h');
                put("0110",       'i');
                put("00111000100",'j');
                put("00111001",   'k');
                put("10110",      'l');
                put("01011",      'm');
                put("1001",       'n');
                put("1010",       'o');
                put("001111",     'p');
                put("00111000101",'q');
                put("0010",       'r');
                put("0100",       's');
                put("1110",       't');
                put("10001",      'u');
                put("0011101",    'v');
                put("101111",     'w');
                put("001110000",  'x');
                put("010101",     'y');
                put("0011100011", 'z');
            }};

        private int cursor_ = 0;
        private String bit_data_ = "";

        public ICTBitstringToPlaintext(String bit_data)
        {
            bit_data_ = bit_data;
        }


        public char getNext()
        {
            StringBuilder key = new StringBuilder();
            if(cursor_ >= bit_data_.length())
                return '?';
            key.append(String.valueOf(bit_data_.charAt(cursor_++)));

            while( !huffman_dict.containsKey(key.toString()) )
            {
                if(cursor_ >= bit_data_.length())
                    return '?';
                key.append(bit_data_.charAt(cursor_++));
            }
            return huffman_dict.get(key.toString());
        }
        public String getString()
        {
            String str = "";
            char c;
            do
            {
                c = getNext();
                if(c != '?')
                {
                    str += c;
                }
            } while(c != '?');
            return str;
        }
    }

    private class ICTSlatToBitstream
    {
        private String slat_string_;
        private String bitstream_;

        private final HashMap<String, String> slat_dict = new HashMap<String, String>(){{
            put("lll","0010");
            put("llr","1101");
            put("llm","00000");
            put("lrl","1100");
            put("lrr","1111");
            put("lrm","00011");
            put("lml","00010");
            put("lmr","01101");
            put("lmm","0000111");
            put("rll","1110");
            put("rlr","1001");
            put("rlm","01100");
            put("rrl","1000");
            put("rrr","1011");
            put("rrm","01111");
            put("rml","01110");
            put("rmr","01001");
            put("rmm","000010");
            put("mll","01000");
            put("mlr","01011");
            put("mlm","001101");
            put("mrl","01010");
            put("mrr","1010");
            put("mrm","001100");
            put("mml","001111");
            put("mmr","001110");
            put("mmm","0000110");
        }};

        public ICTSlatToBitstream(String slat_string)
        {
            slat_string_ = slat_string;
        }

        public void SetSlatString(String slat_string)
        {
            slat_string_ = slat_string;
        }

        public String Convert()
        {
            StringBuilder sb = new StringBuilder();
            for(int i = 3; i <= slat_string_.length(); i+=3)
            {
                sb.append(slat_dict.get(slat_string_.substring(i - 3, i)));
            }
            bitstream_ = sb.toString();
            return bitstream_;
        }

    }
}