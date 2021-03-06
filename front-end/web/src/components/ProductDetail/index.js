import React, { Component } from 'react';
import { Container, Row, Col, Table, Button, Form, FormGroup, Input } from 'reactstrap';
import { FaShoppingCart } from 'react-icons/fa';
import api from '../../services/api';
import ShippingCalculator from '../ShippingCalculator';
import Header from '../Header';
import Footer from '../Footer';

export default class ProductDetail extends Component {
    
        constructor(props){
            super(props);
            this.state = {
                id: 0,
                image: "",
                name: "",
                description: "" ,
                quantity: 1,                
                price: 0,
                height: 0,
                width: 0,
                weight: 0,
                discount: 0,
                balance: 0         
            }                        
        } 

        componentDidMount(){
            this.getProduct();
        }

        getProduct = async() => {

            let id = this.props.match.params.id;

            try {

                    const { data : product } = await api.get("/product/" + id);
                    this.setState({
                        id: product.id,
                        image: product.image,
                        name: product.name,
                        description: product.description,
                        price: product.price,
                        height: product.height,
                        width: product.width,
                        weight: product.weight,
                        discount: product.off,       
                    });
                    const {data: stock } = await api.get("/stock/product/" + id + "/1");
                    this.setState({
                        balance: stock ? stock.balance : 0
                    });

                } catch (error) {

                    this.props.history.push("/NotFound");

            }

        }

        replaceComma = (valor) => {

            return valor.toString().replace(".", ",");

        }
            

        change(event) {

              this.setState({ quantity: event.target.value });      
        };

        handleFormSubmit = (event) => {

            event.preventDefault();

            let cart = JSON.parse(sessionStorage.getItem('cart') || '[]');

            if(cart.length > 0) {

                for (var i in cart) {

                    if(cart[i].id === this.state.id){

                        let  total = parseInt(this.state.quantity) + parseInt(cart[i].quantity);
 
                        if(total <= this.state.balance){

                                cart[i].quantity = total;

                            }else{

                                cart[i].quantity = this.state.balance;

                        }
                                            
                        cart[i].totalItem =  (this.state.price - this.state.price * this.state.discount) *  cart[i].quantity;
                      
                        sessionStorage.cart = JSON.stringify(cart);

                        this.props.history.push("/cart");
                        
                        return;
                    }
                }
            }
   
            cart.push({
                id: this.state.id,
                name: this.state.name,
                image: this.state.image,
                description: this.state.description,
                quantity: parseInt(this.state.quantity),
                price: this.state.price,
                totalItem: (this.state.price - this.state.price * this.state.discount) * this.state.quantity,
                value: this.state.price - this.state.price * this.state.discount,
                balance: this.state.balance

            });

            sessionStorage.setItem('cart', JSON.stringify(cart));
            
            this.props.history.push("/cart");

        };


        render() {
                return (  

                    <>
                    <Header history={this.props.history} location={this.props.location}/>
                    <Container className="pt-5 pb-2 " >
                    <Row className="row">

                        <Col className="mb-3" xs="12" sm="4" md="4" lg="4">
                                <img src={this.state.image} className="rounded" width="100%"
                                    title={this.state.name} alt={this.state.name} />
                        </Col>

                        <Col xs="12"  sm="6" md="6" lg="6">
                            <h3>{this.state.name}</h3>
                            <hr className="soft" />
                            <Form className="form-horizontal qtyFrm">
                                <FormGroup className="control-group">
                                    <h6 className="mb-3"><del>De: {(this.state.price).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}</del></h6>
                                    <h5 className="mb-3">Por: {(this.state.price - this.state.price * this.state.discount).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}</h5>          
                                    <p>Estoque disponível: {this.state.balance}</p>                            
                                    {this.state.balance > 0 ? (
                                            <>
                                                <Input type="number" placeholder="Digite a quantidade" min="1" max={this.state.balance}   value={this.state.quantity} onChange={(e) => this.change(e)} className="col-6 mb-3"  />
                                                <Button color="warning" onClick={this.handleFormSubmit}> <FaShoppingCart/> Comprar</Button>
                                            </>):
                                            (
                                                <Button color="dark" disabled> <FaShoppingCart/> Indisponível</Button>
                                            )

                                    }
                                </FormGroup>
                            </Form>

                            <ShippingCalculator/>

                            <Table className="mt-2">
                                <thead>
                                <tr>
                                <th>Descrição do Produto</th>
                                </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td>
                                        {this.state.description}
                                        </td>
                                    </tr>
                                </tbody>
                            </Table >

                            <Table className="table table-sm">
                                <thead>
                                    <tr>
                                    <th>Altura (m)</th>
                                    <th>Largura (m)</th>
                                    <th>Peso (kg)</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                    <td>{this.replaceComma(this.state.height)}</td>
                                    <td>{this.replaceComma(this.state.width)}</td>
                                    <td>{this.replaceComma(this.state.weight)}</td>                           
                                    </tr>
                                </tbody>
                            </Table>
                                    
                        </Col>
                    </Row>    

                    </Container >
                    
                    <Footer/>

                    </>
               );
       }
}