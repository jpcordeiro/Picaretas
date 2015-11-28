package br.grupointegrado.ads.picaretas.controle;

import br.grupointegrado.ads.picaretas.modelo.Usuario;
import br.grupointegrado.ads.picaretas.modelo.UsuarioDao;
import br.grupointegrado.ads.picaretas.util.Util;
import br.grupointegrado.ads.picaretas.util.ValidacaoUtil;
import java.io.IOException;
import java.sql.Connection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Douglas
 */
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /**
         * recuperando o parametro e o comparando, se igual a sair envalidar a
         * sessao e dispachar o request para o loginServlet
         */

        String sair = req.getParameter("acao");
        if ("sair".equals(sair)) {
            HttpSession sessao = req.getSession();
            sessao.invalidate();
        }
        req.getRequestDispatcher("/WEB-INF/paginas/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String acaoParam = req.getParameter("acao");
            String erro = validaFormulario(req, resp);
            if (erro.isEmpty()) {

                if ("login".equals(acaoParam)) {
                    login(req, resp);
                } else if ("cadastro".equals(acaoParam)) {
                    cadastro(req, resp);
                }
                resp.sendRedirect("Consulta");
            } else {
                req.setAttribute("mensagem_erro", erro);
                doGet(req, resp);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            req.setAttribute("mensagem_erro", "Não foi possível publicar o produto.");
            doGet(req, resp);
        }
    }

    /**
     * Efetua login com o usuário e senha informados.
     *
     * @param req
     * @param resp
     */
    private void login(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String ape = (String) req.getParameter("apelido");
        String sen = (String) req.getParameter("senha");
        req.setAttribute("ape", ape);
        req.setAttribute("sen", sen);

        try {
            String apelido = req.getParameter("apelido");
            String senha = req.getParameter("senha");

            Connection conexao = (Connection) req.getAttribute("conexao");
            UsuarioDao dao = new UsuarioDao(conexao);

            Usuario usuario = dao.consultaEmailSenha(apelido, senha);
            if (usuario != null) {
                // logado
                HttpSession sessao = req.getSession();
                sessao.setAttribute("usuario_logado", usuario);
                resp.sendRedirect("Consulta");
            } else {
                // usuário ou senha incorretos
                req.setAttribute("mensagem_erro", "Apelido/e-mail ou senha incorretos.");
                doGet(req, resp);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            req.setAttribute("mensagem_erro", "Ocorreu um erro inesperado.");
            doGet(req, resp);
        }
    }

    /**
     * Realiza o cadastro do novo usuário no banco de dados.
     *
     * @param req
     * @param resp
     */
    private void cadastro(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // recupero os parâmetros do formulário
            String apelido = req.getParameter("apelido");
            String email = req.getParameter("email");
            String senha = req.getParameter("senha");
            // crio um novo objeto com os parâmetros vindos do formulário
            Usuario usuario = new Usuario();
            usuario.setApelido(apelido);
            usuario.setEmail(email);
            usuario.setSenha(senha);
            // recupero a conexão aberta pelo filtro
            Connection conexao = (Connection) req.getAttribute("conexao");
            // crio uma instância do DAO
            UsuarioDao dao = new UsuarioDao(conexao);
            dao.inserir(usuario);
        } catch (Exception ex) {
            ex.printStackTrace();
            req.setAttribute("mensagem_erro", "Ocorreu um erro inesperado.");
            doGet(req, resp);
        }
    }

    private String validaFormulario(HttpServletRequest req, HttpServletResponse resp) {

        String erro = "";

        String apelidoParam = req.getParameter("apelido");
        if (!ValidacaoUtil.validaString(apelidoParam, 3)) {
            erro += "Necessário um apelido com mais de três caracteres!<br />";
        } else if (!ValidacaoUtil.validaStringMaximo(apelidoParam, 50)) {
            erro += "Campo Apelido superior a 50 caracteres!<br />";
        }

        String emailParam = req.getParameter("email");
        if (!ValidacaoUtil.validaEmail(emailParam)) {
            erro += "Campo E-mail não pode ter caracteres especiais!<br />";
        }

        String senhaParam = req.getParameter("senha");
        if (!ValidacaoUtil.validaSenha(senhaParam)) {
            erro += "Senha deve ter no mínimo 8 caracteres e deve possir letras e números!<br />";
        }

        return erro;
    }
}
