package com.DevArena.backend.problem.service;

import com.DevArena.backend.problem.entity.FunctionParameter;
import com.DevArena.backend.problem.entity.FunctionSignature;
import com.DevArena.backend.problem.enums.ParameterType;
import com.DevArena.backend.problem.enums.SupportedLanguage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates starter code (shown to player) and driver code (hidden, appended before Judge0)
 * for all supported languages from a FunctionSignature.
 *
 * Test case stdin format (one param per "block", in orderIndex order):
 *   INT          → single line: "9"
 *   INT_ARRAY    → single line, space-separated: "2 7 11 15"
 *   INT_MATRIX   → first line = row count, then one row per line: "2\n1 2\n3 4"
 *   STRING       → single line: "hello"
 *   STRING_ARRAY → first line = count, then one string per line: "3\na\nb\nc"
 *   BOOL         → "true" or "false"
 *   FLOAT        → "3.14"
 *   FLOAT_ARRAY  → space-separated: "1.0 2.5 3.7"
 */
@Service
public class DriverGeneratorService {

    // ─────────────────────────────────────────────────────────────────────────
    //  PUBLIC API
    // ─────────────────────────────────────────────────────────────────────────

    public String generateStarterCode(FunctionSignature sig, SupportedLanguage lang) {
        return switch (lang) {
            case PYTHON     -> pythonStarter(sig);
            case JAVA       -> javaStarter(sig);
            case CPP        -> cppStarter(sig);
            case JAVASCRIPT -> jsStarter(sig);
        };
    }

    public String generateDriverCode(FunctionSignature sig, SupportedLanguage lang) {
        return switch (lang) {
            case PYTHON     -> pythonDriver(sig);
            case JAVA       -> javaDriver(sig);
            case CPP        -> cppDriver(sig);
            case JAVASCRIPT -> jsDriver(sig);
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PYTHON
    // ─────────────────────────────────────────────────────────────────────────

    private String pythonStarter(FunctionSignature sig) {
        StringBuilder sb = new StringBuilder();

        // Type hint imports if needed
        boolean needsListHint = sig.getParameters().stream()
                .anyMatch(p -> p.getType() == ParameterType.INT_ARRAY
                        || p.getType() == ParameterType.FLOAT_ARRAY
                        || p.getType() == ParameterType.STRING_ARRAY
                        || p.getType() == ParameterType.INT_MATRIX)
                || sig.getReturnType() == ParameterType.INT_ARRAY
                || sig.getReturnType() == ParameterType.FLOAT_ARRAY
                || sig.getReturnType() == ParameterType.STRING_ARRAY
                || sig.getReturnType() == ParameterType.INT_MATRIX;

        if (needsListHint) {
            sb.append("from typing import List\n\n");
        }

        // Function signature line
        sb.append("def ").append(sig.getFunctionName()).append("(");
        List<FunctionParameter> params = sig.getParameters();
        for (int i = 0; i < params.size(); i++) {
            FunctionParameter p = params.get(i);
            sb.append(p.getName()).append(": ").append(toPythonType(p.getType()));
            if (i < params.size() - 1) sb.append(", ");
        }
        sb.append(") -> ").append(toPythonType(sig.getReturnType())).append(":\n");
        sb.append("    pass\n");

        return sb.toString();
    }

    private String pythonDriver(FunctionSignature sig) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n# ─── Driver (hidden) ───────────────────────────────────\n");
        sb.append("import sys\n");

        // Read each parameter from stdin
        for (FunctionParameter p : sig.getParameters()) {
            sb.append(pythonReadParam(p.getName(), p.getType()));
        }

        // Call function
        String args = sig.getParameters().stream()
                .map(FunctionParameter::getName)
                .collect(Collectors.joining(", "));
        sb.append("_result = ").append(sig.getFunctionName()).append("(").append(args).append(")\n");

        // Print result
        sb.append(pythonPrintResult(sig.getReturnType()));

        return sb.toString();
    }

    private String pythonReadParam(String name, ParameterType type) {
        return switch (type) {
            case INT          -> name + " = int(input())\n";
            case FLOAT        -> name + " = float(input())\n";
            case BOOL         -> name + " = input().strip().lower() == 'true'\n";
            case STRING       -> name + " = input().strip()\n";
            case INT_ARRAY    -> name + " = list(map(int, input().split()))\n";
            case FLOAT_ARRAY  -> name + " = list(map(float, input().split()))\n";
            case STRING_ARRAY ->
                    "_n_" + name + " = int(input())\n" +
                    name + " = [input().strip() for _ in range(_n_" + name + ")]\n";
            case INT_MATRIX   ->
                    "_rows_" + name + " = int(input())\n" +
                    name + " = [list(map(int, input().split())) for _ in range(_rows_" + name + ")]\n";
        };
    }

    private String pythonPrintResult(ParameterType returnType) {
        return switch (returnType) {
            case INT, FLOAT -> "print(_result)\n";
            case BOOL -> "print(str(_result).lower())\n";
            case STRING -> "print(_result)\n";
            case INT_ARRAY, FLOAT_ARRAY -> "print(_result)\n";
            case STRING_ARRAY -> "for _s in _result: print(_s)\n";
            case INT_MATRIX -> "for _row in _result:\n    print(_row)\n";
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  JAVA
    // ─────────────────────────────────────────────────────────────────────────

    private String javaStarter(FunctionSignature sig) {
        StringBuilder sb = new StringBuilder();
        sb.append("import java.util.*;\n\n");
        sb.append("class Solution {\n");
        sb.append("    public ").append(toJavaType(sig.getReturnType()))
          .append(" ").append(sig.getFunctionName()).append("(");

        List<FunctionParameter> params = sig.getParameters();
        for (int i = 0; i < params.size(); i++) {
            FunctionParameter p = params.get(i);
            sb.append(toJavaType(p.getType())).append(" ").append(p.getName());
            if (i < params.size() - 1) sb.append(", ");
        }
        sb.append(") {\n");
        sb.append("        // your code here\n");
        sb.append("        return ").append(javaDefaultReturn(sig.getReturnType())).append(";\n");
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String javaDriver(FunctionSignature sig) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n// ─── Driver (hidden) ────────────────────────────────────\n");
        sb.append("class Main {\n");
        sb.append("    public static void main(String[] args) {\n");
        sb.append("        Scanner sc = new Scanner(System.in);\n");
        sb.append("        Solution sol = new Solution();\n");

        for (FunctionParameter p : sig.getParameters()) {
            sb.append(javaReadParam("sc", p.getName(), p.getType()));
        }

        String args = sig.getParameters().stream()
                .map(FunctionParameter::getName)
                .collect(Collectors.joining(", "));
        sb.append("        ").append(toJavaType(sig.getReturnType()))
          .append(" _result = sol.").append(sig.getFunctionName())
          .append("(").append(args).append(");\n");

        sb.append(javaPrintResult("_result", sig.getReturnType()));
        sb.append("    }\n}\n");
        return sb.toString();
    }

    private String javaReadParam(String sc, String name, ParameterType type) {
        return switch (type) {
            case INT    -> "        int " + name + " = " + sc + ".nextInt();\n";
            case FLOAT  -> "        double " + name + " = " + sc + ".nextDouble();\n";
            case BOOL   -> "        boolean " + name + " = Boolean.parseBoolean(" + sc + ".next());\n";
            case STRING -> "        String " + name + " = " + sc + ".next();\n";
            case INT_ARRAY ->
                "        String[] _" + name + "Parts = " + sc + ".nextLine().trim().split(\"\\\\s+\");\n" +
                "        int[] " + name + " = new int[_" + name + "Parts.length];\n" +
                "        for (int _i = 0; _i < _" + name + "Parts.length; _i++) " +
                name + "[_i] = Integer.parseInt(_" + name + "Parts[_i]);\n";
            case FLOAT_ARRAY ->
                "        String[] _" + name + "Parts = " + sc + ".nextLine().trim().split(\"\\\\s+\");\n" +
                "        double[] " + name + " = new double[_" + name + "Parts.length];\n" +
                "        for (int _i = 0; _i < _" + name + "Parts.length; _i++) " +
                name + "[_i] = Double.parseDouble(_" + name + "Parts[_i]);\n";
            case STRING_ARRAY ->
                "        int _n_" + name + " = Integer.parseInt(" + sc + ".nextLine().trim());\n" +
                "        String[] " + name + " = new String[_n_" + name + "];\n" +
                "        for (int _i = 0; _i < _n_" + name + "; _i++) " +
                name + "[_i] = " + sc + ".nextLine();\n";
            case INT_MATRIX ->
                "        int _rows_" + name + " = Integer.parseInt(" + sc + ".nextLine().trim());\n" +
                "        int[][] " + name + " = new int[_rows_" + name + "][];\n" +
                "        for (int _i = 0; _i < _rows_" + name + "; _i++) {\n" +
                "            String[] _rp = " + sc + ".nextLine().trim().split(\"\\\\s+\");\n" +
                "            " + name + "[_i] = new int[_rp.length];\n" +
                "            for (int _j = 0; _j < _rp.length; _j++) " +
                name + "[_i][_j] = Integer.parseInt(_rp[_j]);\n" +
                "        }\n";
        };
    }

    private String javaPrintResult(String varName, ParameterType type) {
        return switch (type) {
            case INT, FLOAT, BOOL, STRING ->
                "        System.out.println(" + varName + ");\n";
            case INT_ARRAY ->
                "        System.out.println(java.util.Arrays.toString(" + varName + "));\n";
            case FLOAT_ARRAY ->
                "        System.out.println(java.util.Arrays.toString(" + varName + "));\n";
            case STRING_ARRAY ->
                "        System.out.println(java.util.Arrays.toString(" + varName + "));\n";
            case INT_MATRIX ->
                "        for (int[] _row : " + varName + ") " +
                "System.out.println(java.util.Arrays.toString(_row));\n";
        };
    }

    private String javaDefaultReturn(ParameterType type) {
        return switch (type) {
            case INT         -> "0";
            case FLOAT       -> "0.0";
            case BOOL        -> "false";
            case STRING      -> "\"\"";
            case INT_ARRAY   -> "new int[]{}";
            case FLOAT_ARRAY -> "new double[]{}";
            case STRING_ARRAY -> "new String[]{}";
            case INT_MATRIX  -> "new int[][]{}";
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  C++
    // ─────────────────────────────────────────────────────────────────────────

    private String cppStarter(FunctionSignature sig) {
        StringBuilder sb = new StringBuilder();
        sb.append("#include <bits/stdc++.h>\nusing namespace std;\n\n");
        sb.append("class Solution {\npublic:\n");
        sb.append("    ").append(toCppType(sig.getReturnType()))
          .append(" ").append(sig.getFunctionName()).append("(");

        List<FunctionParameter> params = sig.getParameters();
        for (int i = 0; i < params.size(); i++) {
            FunctionParameter p = params.get(i);
            sb.append(toCppType(p.getType())).append(" ").append(p.getName());
            if (i < params.size() - 1) sb.append(", ");
        }
        sb.append(") {\n");
        sb.append("        // your code here\n");
        sb.append("        return ").append(cppDefaultReturn(sig.getReturnType())).append(";\n");
        sb.append("    }\n};\n");
        return sb.toString();
    }

    private String cppDriver(FunctionSignature sig) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n// ─── Driver (hidden) ────────────────────────────────────\n");
        sb.append("int main() {\n");
        sb.append("    ios_base::sync_with_stdio(false);\n");
        sb.append("    cin.tie(NULL);\n");
        sb.append("    Solution sol;\n");

        for (FunctionParameter p : sig.getParameters()) {
            sb.append(cppReadParam(p.getName(), p.getType()));
        }

        String args = sig.getParameters().stream()
                .map(FunctionParameter::getName)
                .collect(Collectors.joining(", "));
        sb.append("    auto _result = sol.").append(sig.getFunctionName())
          .append("(").append(args).append(");\n");

        sb.append(cppPrintResult("_result", sig.getReturnType()));
        sb.append("    return 0;\n}\n");
        return sb.toString();
    }

    private String cppReadParam(String name, ParameterType type) {
        return switch (type) {
            case INT    -> "    int " + name + "; cin >> " + name + ";\n";
            case FLOAT  -> "    double " + name + "; cin >> " + name + ";\n";
            case BOOL   -> "    string _b_" + name + "; cin >> _b_" + name + ";\n" +
                           "    bool " + name + " = (_b_" + name + " == \"true\");\n";
            case STRING -> "    string " + name + "; cin >> " + name + ";\n";
            case INT_ARRAY ->
                "    string _line_" + name + "; getline(cin, _line_" + name + ");\n" +
                "    vector<int> " + name + ";\n" +
                "    { istringstream _iss(_line_" + name + ");\n" +
                "      int _v; while(_iss >> _v) " + name + ".push_back(_v); }\n";
            case FLOAT_ARRAY ->
                "    string _line_" + name + "; getline(cin, _line_" + name + ");\n" +
                "    vector<double> " + name + ";\n" +
                "    { istringstream _iss(_line_" + name + ");\n" +
                "      double _v; while(_iss >> _v) " + name + ".push_back(_v); }\n";
            case STRING_ARRAY ->
                "    int _n_" + name + "; cin >> _n_" + name + "; cin.ignore();\n" +
                "    vector<string> " + name + "(_n_" + name + ");\n" +
                "    for(int _i=0;_i<_n_" + name + ";_i++) getline(cin," + name + "[_i]);\n";
            case INT_MATRIX ->
                "    int _rows_" + name + "; cin >> _rows_" + name + "; cin.ignore();\n" +
                "    vector<vector<int>> " + name + "(_rows_" + name + ");\n" +
                "    for(int _i=0;_i<_rows_" + name + ";_i++){\n" +
                "        string _rl; getline(cin,_rl);\n" +
                "        istringstream _iss(_rl); int _v;\n" +
                "        while(_iss>>_v) " + name + "[_i].push_back(_v);\n" +
                "    }\n";
        };
    }

    private String cppPrintResult(String varName, ParameterType type) {
        return switch (type) {
            case INT, FLOAT, BOOL, STRING ->
                "    cout << " + varName + " << endl;\n";
            case INT_ARRAY, FLOAT_ARRAY ->
                "    cout << \"[\";\n" +
                "    for(int _i=0;_i<(int)" + varName + ".size();_i++){\n" +
                "        if(_i) cout << \", \";\n" +
                "        cout << " + varName + "[_i];\n" +
                "    }\n    cout << \"]\" << endl;\n";
            case STRING_ARRAY ->
                "    cout << \"[\";\n" +
                "    for(int _i=0;_i<(int)" + varName + ".size();_i++){\n" +
                "        if(_i) cout << \", \";\n" +
                "        cout << " + varName + "[_i];\n" +
                "    }\n    cout << \"]\" << endl;\n";
            case INT_MATRIX ->
                "    for(auto& _row : " + varName + "){\n" +
                "        cout << \"[\";\n" +
                "        for(int _i=0;_i<(int)_row.size();_i++){\n" +
                "            if(_i) cout << \", \";\n" +
                "            cout << _row[_i];\n" +
                "        }\n" +
                "        cout << \"]\" << endl;\n" +
                "    }\n";
        };
    }

    private String cppDefaultReturn(ParameterType type) {
        return switch (type) {
            case INT         -> "0";
            case FLOAT       -> "0.0";
            case BOOL        -> "false";
            case STRING      -> "\"\"";
            case INT_ARRAY   -> "{}";
            case FLOAT_ARRAY -> "{}";
            case STRING_ARRAY -> "{}";
            case INT_MATRIX  -> "{}";
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  JAVASCRIPT
    // ─────────────────────────────────────────────────────────────────────────

    private String jsStarter(FunctionSignature sig) {
        StringBuilder sb = new StringBuilder();
        sb.append("/**\n");
        for (FunctionParameter p : sig.getParameters()) {
            sb.append(" * @param {").append(toJsType(p.getType())).append("} ").append(p.getName()).append("\n");
        }
        sb.append(" * @return {").append(toJsType(sig.getReturnType())).append("}\n");
        sb.append(" */\n");
        sb.append("var ").append(sig.getFunctionName()).append(" = function(");
        List<FunctionParameter> params = sig.getParameters();
        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).getName());
            if (i < params.size() - 1) sb.append(", ");
        }
        sb.append(") {\n    // your code here\n};\n");
        return sb.toString();
    }

    private String jsDriver(FunctionSignature sig) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n// ─── Driver (hidden) ────────────────────────────────────\n");
        sb.append("const readline = require('readline');\n");
        sb.append("const rl = readline.createInterface({ input: process.stdin });\n");
        sb.append("const _lines = [];\n");
        sb.append("rl.on('line', l => _lines.push(l.trim()));\n");
        sb.append("rl.on('close', () => {\n");
        sb.append("    let _idx = 0;\n");

        for (FunctionParameter p : sig.getParameters()) {
            sb.append(jsReadParam(p.getName(), p.getType()));
        }

        String args = sig.getParameters().stream()
                .map(FunctionParameter::getName)
                .collect(Collectors.joining(", "));
        sb.append("    const _result = ").append(sig.getFunctionName())
          .append("(").append(args).append(");\n");

        sb.append(jsPrintResult("_result", sig.getReturnType()));
        sb.append("});\n");
        return sb.toString();
    }

    private String jsReadParam(String name, ParameterType type) {
        return switch (type) {
            case INT    -> "    const " + name + " = parseInt(_lines[_idx++]);\n";
            case FLOAT  -> "    const " + name + " = parseFloat(_lines[_idx++]);\n";
            case BOOL   -> "    const " + name + " = _lines[_idx++] === 'true';\n";
            case STRING -> "    const " + name + " = _lines[_idx++];\n";
            case INT_ARRAY ->
                "    const " + name + " = _lines[_idx++].split(' ').map(Number);\n";
            case FLOAT_ARRAY ->
                "    const " + name + " = _lines[_idx++].split(' ').map(parseFloat);\n";
            case STRING_ARRAY ->
                "    const _n_" + name + " = parseInt(_lines[_idx++]);\n" +
                "    const " + name + " = [];\n" +
                "    for(let _i=0;_i<_n_" + name + ";_i++) " + name + ".push(_lines[_idx++]);\n";
            case INT_MATRIX ->
                "    const _rows_" + name + " = parseInt(_lines[_idx++]);\n" +
                "    const " + name + " = [];\n" +
                "    for(let _i=0;_i<_rows_" + name + ";_i++) " +
                name + ".push(_lines[_idx++].split(' ').map(Number));\n";
        };
    }

    private String jsPrintResult(String varName, ParameterType type) {
        return switch (type) {
            case INT, FLOAT, BOOL, STRING ->
                "    console.log(" + varName + ");\n";
            case INT_ARRAY, FLOAT_ARRAY, STRING_ARRAY ->
                "    console.log(JSON.stringify(" + varName + "));\n";
            case INT_MATRIX ->
                "    " + varName + ".forEach(r => console.log(JSON.stringify(r)));\n";
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TYPE MAPPING HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private String toPythonType(ParameterType type) {
        return switch (type) {
            case INT          -> "int";
            case FLOAT        -> "float";
            case BOOL         -> "bool";
            case STRING       -> "str";
            case INT_ARRAY    -> "List[int]";
            case FLOAT_ARRAY  -> "List[float]";
            case STRING_ARRAY -> "List[str]";
            case INT_MATRIX   -> "List[List[int]]";
        };
    }

    private String toJavaType(ParameterType type) {
        return switch (type) {
            case INT          -> "int";
            case FLOAT        -> "double";
            case BOOL         -> "boolean";
            case STRING       -> "String";
            case INT_ARRAY    -> "int[]";
            case FLOAT_ARRAY  -> "double[]";
            case STRING_ARRAY -> "String[]";
            case INT_MATRIX   -> "int[][]";
        };
    }

    private String toCppType(ParameterType type) {
        return switch (type) {
            case INT          -> "int";
            case FLOAT        -> "double";
            case BOOL         -> "bool";
            case STRING       -> "string";
            case INT_ARRAY    -> "vector<int>";
            case FLOAT_ARRAY  -> "vector<double>";
            case STRING_ARRAY -> "vector<string>";
            case INT_MATRIX   -> "vector<vector<int>>";
        };
    }

    private String toJsType(ParameterType type) {
        return switch (type) {
            case INT, FLOAT   -> "number";
            case BOOL         -> "boolean";
            case STRING       -> "string";
            case INT_ARRAY, FLOAT_ARRAY -> "number[]";
            case STRING_ARRAY -> "string[]";
            case INT_MATRIX   -> "number[][]";
        };
    }
}