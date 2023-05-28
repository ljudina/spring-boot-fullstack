import {Center, Spinner, Text, Wrap, WrapItem} from '@chakra-ui/react'
import SidebarWithHeader from './components/shared/Sidebar.jsx'
import {useEffect, useState} from 'react';
import {getCustomers} from "./services/client.js";
import CardWithImage from "./components/CardWithImage.jsx";
import CreateCustomerDrawer from "./components/CreateCustomerDrawer.jsx";
import {errorNotification} from "./services/notification.js";

const App = () => {
    const [customers, setCustomers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const fetchCustomers = () => {
        getCustomers().then(res => {
            setCustomers(res.data);
        }).catch(err => {
            setError(err.response.data.message);
            errorNotification(
                err.code,
                err.response.data.message
            );
        }).finally(() => {
            setLoading(false);
        })
    }
    useEffect(() => {
        fetchCustomers();
    }, []);

    if(error){
        return (
            <SidebarWithHeader>
                <CreateCustomerDrawer fetchCustomers={fetchCustomers} />
                <Text mt={5}>Oops.. There was an error</Text>
            </SidebarWithHeader>
        )
    }

    if(loading){
        return (
            <SidebarWithHeader>
                <Spinner
                    thickness='4px'
                    speed='0.65s'
                    emptyColor='gray.200'
                    color='blue.500'
                    size='xl'
                />
            </SidebarWithHeader>
        )
    }
    if(customers.length <= 0){
        return (
            <SidebarWithHeader>
                <CreateCustomerDrawer fetchCustomers={fetchCustomers} />
                <Text mt={5}>No customers available</Text>
            </SidebarWithHeader>
        );
    }
    return (
        <SidebarWithHeader>
            <CreateCustomerDrawer fetchCustomers={fetchCustomers} />
            <Wrap spacing={"15px"} mt={"10px"}>
            {customers.map((customer, index) => (
                <WrapItem key={index} justify={"center"}>
                    <CardWithImage
                        {...customer}
                        imageNumber={customer.id}
                        fetchCustomers={fetchCustomers}
                    />
                </WrapItem>
            ))}
            </Wrap>
        </SidebarWithHeader>
    )
}

export default App;