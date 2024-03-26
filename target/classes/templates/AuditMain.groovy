package templates

import br.com.suporte.moovinAudit.jdbc.JdbcTemplate
import br.com.suporte.moovinAudit.rest.RestTemplate
import com.opencsv.CSVWriter

import java.nio.file.Files
import java.nio.file.Paths


JdbcTemplate jdbcTemplate = new JdbcTemplate();

int utilizador
String searchpath
String loja
String token

String methos = "POST"

def arquivoConfiguracao = "/home/maker/config.txt"

if (Files.exists(Paths.get(arquivoConfiguracao))) {
    def linhas = Files.readAllLines(Paths.get(arquivoConfiguracao))

    linhas.each { linha ->
        def partes = linha.split("=")

        switch (partes[0].trim()) {
            case "utilizador":
                utilizador = Integer.parseInt(partes[1].trim())
                break
            case "searchpath":
                searchpath = partes[1].trim()
                break
            case "loja":
                loja = partes[1].trim()
                break
            case "token":
                token = partes[1].trim()
                break
            default:
                break
        }
    }
} else {
    println "Arquivo de configuração não encontrado: $arquivoConfiguracao"
}

String query =
        "select sku.codigo\n" +
                "from mkr_hub_produto_sku_armazem armaze\n" +
                "              join mkr_hub_produto_sku sku on armaze.id_hub_produto_sku = sku.id\n" +
                "where armaze.saldo > 0"

def produtosAuditados = []

List result = jdbcTemplate.jdbcTemplateConsultaComQueryNativaPostgresListaResultadoOneRow(searchpath, query)

if (result) {
    for (produto in result) {
        String url = "http://${loja}.integration.moovin.com.br/webservice/getQuantidadeEstoque/json"
        def data = "{\"produto_codigo_tamanho\": \"${produto.toString()}\"}"
        def body = [
                'token': token,
                'data' : data.toString()
        ]
        println("Verificando produto -> " + produto)
        Map<String, Object> response = RestTemplate.sendRequest(url, methos, body)

        if (response != null) {
            List saldoHub = consultarSaldoHub(produto.toString(), searchpath)
            List estoques = response.get("estoques")
            int saldoMoovinTotal = 0
            int saldoHubTotal = 0

            estoques.each { qtd -> saldoMoovinTotal += qtd.get("qtd") as int }
            saldoHub.each { qtd -> saldoHubTotal += qtd.getColumn1() as int }

            if (saldoHubTotal != saldoMoovinTotal) {
                createlogentidade(produto.toString(), utilizador)
                produtosAuditados.add(produto.toString())
                generateCsvReport(['produto auditado': produtosAuditados], searchpath, "none")
            } else {
                println("produto ${produto.toString()} esta correto!")
            }
        }
        Thread.sleep(1000)
    }
}

if (produtosAuditados) {
    generateCsvReport(['produto auditado': produtosAuditados], utilizador, "none")
}

private static List consultarSaldoHub(String sku, String utilizador) {
    JdbcTemplate jdbcTemplate = new JdbcTemplate();
    List<String> result = new ArrayList<>();
    String query =
            "select amz.saldo ::varchar as saldo\n " +
                    " ,lojaMoovin.lojaMoovin::varchar as lojamoovin\n " +
                    "  from mkr_hub_produto_sku_armazem amz\n " +
                    "         join mkr_hub_produto_sku sku on sku.id = amz.id_hub_produto_sku\n " +
                    "         JOIN mkr_hub_produto_sku_integ_sis integsis on sku.id = integsis.id_hub_produto_sku\n " +
                    "         join lateral (select split_part(ieearmazem.tabela_origem_pk, ',', 1) as lojaMoovin\n " +
                    "                       from mkr_integracao_entidade_erp ieearmazem\n " +
                    "                       where 1 = 1\n " +
                    "                         and tabela_erp = 'MKR_HUB_ARMAZEM'\n " +
                    "                         and tabela_origem = 'lojas'\n " +
                    "                         and ieearmazem.tabela_erp_pk = amz.id_hub_armazem\n " +
                    "    ) as lojaMoovin on true\n " +
                    " where sku.codigo = '${sku}' "
    List<JdbcTemplate.ResultQuery> saldoHub = jdbcTemplate.jdbcTemplateConsultaComQueryNativaPostgresListaResultadoTwoRow(utilizador, query, "saldo", "lojamoovin")
    result.add(saldoHub.each { column -> column.getColumn1().toString() } as String)
    return saldoHub
}

private static void createlogentidade(String sku, int utilizador) {
    String logentidade = "with sku as (\n" +
            "    select id from utilizador_0000000360.mkr_hub_produto_sku where codigo in ('${sku}')\n" +
            " )\n" +
            " INSERT INTO mkr_log_entidade (id, id_entidade, dh_registro, tabela, state, id_utilizador)\n" +
            "    (SELECT nextval('public.gen_mkr_log_entidade'),\n" +
            "            73872561,\n" +
            "            current_timestamp at time zone 'america/sao_paulo',\n" +
            "            'MKR_HUB_PRODUTO_SKU_ARMAZEM',\n" +
            "            'MODIFIED',\n" +
            "            ${utilizador as int})"
    JdbcTemplate jdbcTemplate = new JdbcTemplate();
    jdbcTemplate.jdbcInsert(logentidade)
}

private static void generateCsvReport(Map informationToReport, String utzString, String setaErpRede) {
    String filePath = "./reports/utilizador_${utzString}-${setaErpRede}.csv"
    CSVWriter writer = new CSVWriter(new FileWriter(new File(filePath)))

    if (informationToReport.size() <= 0) {
        return
    }

    informationToReport.eachWithIndex { mapKey, mapValue, Integer mapIndex ->
        String[] sectionTitle = [mapKey as String]
        List fileHeaders = []
        List<String[]> resultSet = new ArrayList<String[]>()
        List temp = []

        for (Integer position = 0; position < mapValue.size(); position++) {
            temp.clear()
            mapValue.getAt(position).eachWithIndex { productInfoKey, productInfoValue, Integer productInfoIndex ->
                if (position == 0) {
                    fileHeaders.add(productInfoKey as String)
                }

                temp.add(productInfoValue as String)
            }
            resultSet.add(temp as String[])
        }

        writer.writeNext(sectionTitle)
        writer.writeNext()
        writer.writeNext(fileHeaders as String[])
        writer.writeAll(resultSet)
        writer.writeNext()
        writer.writeNext()

        resultSet.clear()
    }

    writer.close()
}




