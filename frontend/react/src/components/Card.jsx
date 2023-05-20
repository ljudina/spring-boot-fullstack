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
    Wrap, WrapItem
} from '@chakra-ui/react';

export default function CardWithImage({id, name, email, age, gender, imageNumber}) {
    const randomUserGender = gender === "MALE" ? "men" : "women";
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
                        src={
                            `https://randomuser.me/api/portraits/med/${randomUserGender}/${imageNumber}.jpg`
                        }
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
                    </Stack>
                </Box>
            </Box>
        </Center>
    );
}