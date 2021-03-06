package com.otaviotarelho.curso.services;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.otaviotarelho.curso.domain.Cliente;
import com.otaviotarelho.curso.domain.ItemPedido;
import com.otaviotarelho.curso.domain.PagamentoComBoleto;
import com.otaviotarelho.curso.domain.Pedido;
import com.otaviotarelho.curso.domain.enums.EstadoPagamento;
import com.otaviotarelho.curso.repositories.ClienteRepository;
import com.otaviotarelho.curso.repositories.ItemPedidoRepository;
import com.otaviotarelho.curso.repositories.PagamentoRepository;
import com.otaviotarelho.curso.repositories.PedidoRespository;
import com.otaviotarelho.curso.repositories.ProdutoRepository;
import com.otaviotarelho.curso.security.UserSpringSecurity;
import com.otaviotarelho.curso.services.exceptions.AuthorizationException;
import com.otaviotarelho.curso.services.exceptions.ObjectNotFoundException;

@Service
public class PedidoService {
	
	@Autowired
	private PedidoRespository repo;
	
	@Autowired
	private BoletoService boletoService;
	
	@Autowired
	private PagamentoRepository pagamentoRepository;
	
	@Autowired
	private ProdutoRepository produtoRepository;
	
	@Autowired
	private ItemPedidoRepository itemPedidoRepository;
	
	@Autowired
	private ClienteRepository clienteRepository;
	
	@Autowired
	private EmailService emailService;
	
	public Pedido find(Integer id) {
		Pedido obj = repo.findOne(id);
		
		if(obj == null) {
			throw new ObjectNotFoundException("object não encontrado");
		}
		
		return obj;
	}

	public Pedido insert(Pedido obj) {
		obj.setId(null);
		obj.setInstante(new Date());
		obj.getPagamento().setEstado(EstadoPagamento.PENDENTE);
		obj.getPagamento().setPedido(obj);
		obj.setCliente(clienteRepository.findOne(obj.getCliente().getId()));
		if(obj.getPagamento() instanceof PagamentoComBoleto) {
			PagamentoComBoleto pagto = (PagamentoComBoleto) obj.getPagamento();
			boletoService.preencherPagamentoComBoleto(pagto, obj.getInstante());
		}
		obj = repo.save(obj);
		
		pagamentoRepository.save(obj.getPagamento());
		
		for(ItemPedido item : obj.getItens()) {
			item.setDesconto(0.00);
			item.setProduto(produtoRepository.findOne(item.getProduto().getId()));
			item.setPreco(item.getProduto().getPreco());
			item.setPedido(obj);
		}
		
		itemPedidoRepository.save(obj.getItens());
		
		emailService.sendOrderConfirmationHtmlEmail(obj);
		
		return obj;
	}
	
	public Page<Pedido> findPage(Integer page, Integer linePerPage, String orderBy, String direction){
		
		UserSpringSecurity user = UserService.authenticated();
		if(user == null) {
			throw new AuthorizationException("Acesso Negado");
		}
		
		PageRequest pageRequest = new PageRequest(page, linePerPage, Direction.valueOf(direction), orderBy);
		
		Cliente cliente = clienteRepository.findOne(user.getId());
		
		return repo.findByCliente(cliente, pageRequest);
	}
}
