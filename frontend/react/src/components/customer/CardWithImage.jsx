import {
    Heading,
    Avatar,
    Box,
    Center,
    Image,
    Flex,
    Text,
    Stack,
    Tag,
    useColorModeValue,
    Wrap, WrapItem,
    Button, useDisclosure, AlertDialog, AlertDialogOverlay, AlertDialogContent, AlertDialogHeader, AlertDialogBody,
    AlertDialogFooter
} from '@chakra-ui/react';
import {DeleteIcon, EditIcon} from '@chakra-ui/icons';
import {useRef} from "react";
import {customerProfilePictureUrl, deleteCustomer} from "../../services/client.js";
import {errorNotification, successNotification} from "../../services/notification.js";
import UpdateCustomerDrawer from "./UpdateCustomerDrawer.jsx";

export default function CardWithImage({id, name, email, age, gender, imageNumber, fetchCustomers}) {
    const randomUserGender = gender === "MALE" ? "men" : "women";
    const { isOpen, onOpen, onClose } = useDisclosure()
    const cancelRef = useRef();
    const deleteSpecificCustomer = (id) => {
        deleteCustomer(id, name)
            .then(res => {
                successNotification(
                    "Customer deleted",
                    `Customer ${name} was successfully deleted!`
                );
                fetchCustomers();
            })
            .catch(err => {
                errorNotification(
                    err.code,
                    err.response.data.message
                );
            })
    }
    return (
        <Center py={0}>
            <Box
                maxW={'300px'}
                w={'full'}
                bg={useColorModeValue('white', 'gray.800')}
                boxShadow={'2xl'}
                rounded={'md'}
                overflow={'hidden'}>
                <Image
                    h={'120px'}
                    w={'full'}
                    src={
                        'https://images.unsplash.com/photo-1612865547334-09cb8cb455da?ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&ixlib=rb-1.2.1&auto=format&fit=crop&w=634&q=80'
                    }
                    objectFit={'cover'}
                />
                <Flex justify={'center'} mt={-12}>
                    <Avatar
                        size={'xl'}
                        src={customerProfilePictureUrl(id)}
                        alt={'Author'}
                        css={{
                            border: '2px solid white',
                        }}
                    />
                </Flex>

                <Box p={6}>
                    <Stack spacing={2} align={'center'} mb={5}>
                        <Tag borderRadius={"full"}>{id}</Tag>
                        <Heading fontSize={'2xl'} fontWeight={500} fontFamily={'body'}>
                            {name}
                        </Heading>
                        <Text color={'gray.500'}>{email}</Text>
                        <Wrap spacing={"15px"}>
                            <WrapItem justify={"center"}>
                            <Text color={'gray.500'}>Age {age}</Text>
                            </WrapItem>
                            <WrapItem justify={"center"}>
                                |
                            </WrapItem>
                            <WrapItem justify={"center"}>
                            <Text color={'gray.500'}>{gender}</Text>
                            </WrapItem>
                        </Wrap>
                        <Wrap spacing={"15px"}>
                            <WrapItem>
                                <UpdateCustomerDrawer fetchCustomers={fetchCustomers} initialValues={{name, email, age}} customerId={id} />
                            </WrapItem>
                            <WrapItem>
                                <Button onClick={onOpen}>
                                    <DeleteIcon color="red.500" />
                                </Button>
                            </WrapItem>
                        </Wrap>
                        <AlertDialog
                            isOpen={isOpen}
                            leastDestructiveRef={cancelRef}
                            onClose={onClose}
                        >
                            <AlertDialogOverlay>
                                <AlertDialogContent>
                                    <AlertDialogHeader fontSize='lg' fontWeight='bold'>
                                        Delete Customer
                                    </AlertDialogHeader>

                                    <AlertDialogBody>
                                        Are you sure? You can not undo this action afterwards.
                                    </AlertDialogBody>

                                    <AlertDialogFooter>
                                        <Button ref={cancelRef} onClick={onClose}>
                                            Cancel
                                        </Button>
                                        <Button
                                            colorScheme='red'
                                            ml={3}
                                            onClick={() => deleteSpecificCustomer(id, name)}
                                        >
                                            Delete
                                        </Button>
                                    </AlertDialogFooter>
                                </AlertDialogContent>
                            </AlertDialogOverlay>
                        </AlertDialog>
                    </Stack>
                </Box>
            </Box>
        </Center>
    );
}