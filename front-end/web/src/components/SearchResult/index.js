import React, { Component } from 'react';
import api from '../../services/api';
import Header from '../Header';

import {
    Container,
    Col,
    Row,
    Card
} from 'reactstrap';


export default class SearchResult extends Component {

    constructor(props) {
        super(props);
        this.state = {
            products: []
        }
    }

    componentDidMount(){
        this.getResult();
    }

    getResult = async () => {
        try {
            const { data: products } = await api.get("/product/find/" + this.props.match.params.product);
            if (!products)
                return;
            this.setState({ products });
        } catch{
            return;
        }

    }

    redirect = (evt) => {
        let obj = evt.target;
        while (obj.id !== "card")
            obj = obj.parentNode;
        this.props.history.push(`/detail/${obj.children[1].innerHTML.toString()}`);

    }

    render() {
        return (
            <>
                <Header history={this.props.history} location={this.props.location} />
                {this.state.products.length > 0 ? (
                    <Container>
                        <Row>
                            {this.state.products.map(p => (
                                <Card className="cursor-pointer p-1 mb-2" id="card" onClick={this.redirect}>
                                    <Row className="d-flex align-items-center ">
                                        <Col md="3">
                                            <img src={p.image} alt={p.name} title={p.name} />
                                        </Col>
                                        <Col md="7" >
                                            <p className="h4">{p.name}</p>
                                            <p>
                                                {p.description.substring(0, 200) + "..."}
                                            </p>
                                        </Col>
                                        <Col md="2">
                                            <p className="h6">De: <del>{(p.price).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}</del></p>
                                            <p className="h5">Por: {(p.price - p.price * p.off).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}</p>
                                        </Col>
                                    </Row>
                                    <spam className="d-none">{p.id}</spam>
                                </Card>
                            ))}
                        </Row>
                    </Container>
                ) :
                    (
                        <Container className="text-center">
                            <span className="h2">Nenhum produto encontrado</span>
                        </Container>
                    )}
            </>
        )
    }
}
