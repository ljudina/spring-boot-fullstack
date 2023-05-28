import { Button, Drawer, DrawerOverlay, DrawerContent, DrawerCloseButton, DrawerHeader, DrawerBody, DrawerFooter } from "@chakra-ui/react";
import { useDisclosure } from "@chakra-ui/react";
import {EditIcon} from "@chakra-ui/icons";
import UpdateCustomerFrom from "./UpdateCustomerFrom.jsx";
const CloseIcon = () => "x";
const UpdateCustomerDrawer = ({ fetchCustomers, initialValues, customerId }) => {
    const { isOpen, onOpen, onClose } = useDisclosure()
    return <>
        <Button onClick={onOpen}>
            <EditIcon />
        </Button>
        <Drawer isOpen={isOpen} onClose={onClose} size={"xl"}>
            <DrawerOverlay />
            <DrawerContent>
                <DrawerCloseButton />
                <DrawerHeader>Update Customer</DrawerHeader>

                <DrawerBody>
                    <UpdateCustomerFrom fetchCustomers={fetchCustomers} initialValues={initialValues} customerId={customerId} drawerClose={onClose}/>
                </DrawerBody>

                <DrawerFooter>
                    <Button
                        leftIcon={<CloseIcon />}
                        colorScheme={"teal"}
                        onClick={onClose}
                    >
                        Close
                    </Button>
                </DrawerFooter>
            </DrawerContent>
        </Drawer>
    </>
}
export default UpdateCustomerDrawer;
