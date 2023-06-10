import { Button, Drawer, DrawerOverlay, DrawerContent, DrawerCloseButton, DrawerHeader, DrawerBody, DrawerFooter } from "@chakra-ui/react";
import { useDisclosure } from "@chakra-ui/react";
import CreateCustomerForm from "../shared/CreateCustomerFrom.jsx";
const AddIcon = () => "+";
const CloseIcon = () => "x";
const CreateCustomerDrawer = ({ fetchCustomers }) => {
    const { isOpen, onOpen, onClose } = useDisclosure()
    return <>
        <Button
            leftIcon={<AddIcon />}
            colorScheme={"teal"}
            onClick={onOpen}
        >
            Create customer
        </Button>
        <Drawer isOpen={isOpen} onClose={onClose} size={"xl"}>
            <DrawerOverlay />
            <DrawerContent>
                <DrawerCloseButton />
                <DrawerHeader>Create your account</DrawerHeader>

                <DrawerBody>
                    <CreateCustomerForm onSuccess={fetchCustomers}/>
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
export default CreateCustomerDrawer;
