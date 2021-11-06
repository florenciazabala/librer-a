package com.egg.library.domain.service;

import com.egg.library.domain.AuthorVO;
import com.egg.library.domain.repository.AuthorVORepository;
import com.egg.library.exeptions.FieldAlreadyExistException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AuthorServiceTest {

    @Mock
    private AuthorVORepository authorVORepository;

    @InjectMocks
    private AuthorService authorService;

    List<AuthorVO> authorsList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authorsList = getAuthorsLists();
    }

    public List<AuthorVO> getAuthorsLists(){
        List<AuthorVO> authors = new ArrayList<>();
        authors.add(new AuthorVO(1,"Cortazar",true));
        authors.add(new AuthorVO(2,"Borges",false));
        authors.add(new AuthorVO(3,"Ray Bradbury",true));
        authors.add(new AuthorVO(4,"Stephen King",true));
        return authors;
    }

    @Test
    void createAuthorWhenExistsByNameIsTrue() {
        String name = "Cortazar";
        when(authorVORepository.existsByName(any(String.class))).thenReturn(true);
        FieldAlreadyExistException myException = null;
        try {
            authorService.createAuthor(name);
        } catch (FieldAlreadyExistException e) {
            myException = e;
        }
        assertEquals(myException.getMessage(), "Conflict Exception (409). Field Already Exist Exception . The author with name '"+name+"' already exists");
        //assertEquals(myException.getClass(), FieldAlreadyExistException.class);
    }

    @Test
    void createAuthorWhenExistsByNameIsFalse() {
        String name = "Cortazar";
        when(authorVORepository.existsByName(any(String.class))).thenReturn(false);
        FieldAlreadyExistException myException = null;
        try {
            authorService.createAuthor(name);
        } catch (FieldAlreadyExistException e) {
            myException = e;
        }
        assertNull(myException);
    }

    @Test
    void createAuthorWhenExistsByNameIsFalseAndReurnObject() {
        String name = "Cortazar";
        when(authorVORepository.existsByName(any(String.class))).thenReturn(false);
        when(authorVORepository.create(any(AuthorVO.class))).thenReturn(authorsList.get(0));
        assertEquals(authorsList.get(0),authorService.createAuthor(name));
    }

    @Test
    void updateAuthorWhenIdDoesntExists() {
        String name = "Borges";
        Integer id = 2;
        when(authorVORepository.getById(any(Integer.class))).thenReturn(Optional.empty());
        Exception exception = assertThrows(NoSuchElementException.class,()->{
            authorService.updateAuthor(name,id);
        });

        String expectedMessage = "The author with id '"+id+"' doesn't exists";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void updateAuthorWhenTheNameUpdateAlreaadyExists() {
        String name = "Borges";
        when(authorVORepository.getById(any(Integer.class))).thenReturn(Optional.of(authorsList.get(1)));
        when(authorVORepository.getByName(any(String.class))).thenReturn(authorsList.get(2));
        Exception exception = assertThrows(FieldAlreadyExistException.class,()->{
            authorService.updateAuthor(name,2);
        });

        String expectedMessage = "The author with name '"+name+"' already exists";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void findAllAuthors() {
        when(authorVORepository.getAll()).thenReturn(getAuthorsLists());
        List<AuthorVO> authorsList = authorService.findAllAuthors();
        assertNotNull(authorsList);
        assertTrue(authorsList.size()>0);

    }

    @Test
    void findByName() {
    }

    //Find by id
    @Test
    void findById_when_author_find_is_null() {
        Optional<AuthorVO> authorVO = Optional.empty();
        when(authorVORepository.getById(any(Integer.class))).thenReturn(authorVO);
        Exception myException = null;
        try {
            authorService.findById(3);
        } catch (Exception e) {
            myException = e;
        }
        assertEquals(myException.getMessage(), "The author with id '3' doesn't exists");
    }

    @Test
    void findById_when_author_is_find() {
        when(authorVORepository.getById(any(Integer.class))).thenReturn(Optional.of(authorsList.get(2)));
        Exception myException = null;
        assertEquals(authorsList.get(2), authorService.findById(2));
    }

    //Delete

    @Test
    void delete_when_id_does_not_exists() {
        Integer id =1;
        when(authorVORepository.getById(any(Integer.class))).thenReturn(Optional.empty());
        Exception exception = null;
        try {
            authorService.delete(id);
        }catch (Exception e){
            exception = e;
        }
        assertEquals(exception.getMessage(),"Conflict Exception (409). Field Already Exist Exception . The author with id '"+id+"' doesn't exists");
    }

    @Test
    void delete_when_id_does_not_exists_2() {
        Integer id =1;
        when(authorVORepository.getById(any(Integer.class))).thenReturn(Optional.empty());
        Exception exception = assertThrows(FieldAlreadyExistException.class,()->{
            authorService.delete(id);
        });

        String expectedMessage = "The author with id '"+id+"' doesn't exists";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void delete_set_discharged_false_when_its_true() {
        Integer id =1;
        when(authorVORepository.getById(any(Integer.class))).thenReturn(Optional.of(authorsList.get(0)));
        AuthorVO authorVO = authorVORepository.getById(id).get();
        authorService.delete(id);
        assertFalse(authorVO.getDischarged());
    }

    @Test
    void delete_keep_discharged_false_when_its_false() {
        Integer id =1;
        when(authorVORepository.getById(any(Integer.class))).thenReturn(Optional.of(authorsList.get(1)));
        AuthorVO authorVO = authorVORepository.getById(id).get();
        authorService.delete(id);
        assertFalse(authorVO.getDischarged());
    }

    //Discharge

    @Test
    void discharge_when_id_does_not_exists() {
        Integer id =1;
        when(authorVORepository.getById(any(Integer.class))).thenReturn(Optional.empty());
        Exception exception = assertThrows(FieldAlreadyExistException.class,()->{
            authorService.discharge(id);
        });

        String expectedMessage = "The author with id '"+id+"' doesn't exists";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void discharge_set_discharged_true_when_its_false() {
        Integer id =1;
        when(authorVORepository.getById(any(Integer.class))).thenReturn(Optional.of(authorsList.get(1)));
        AuthorVO authorVO = authorVORepository.getById(id).get();
        authorService.discharge(id);
        assertTrue(authorVO.getDischarged());
    }

    @Test
    void discharge_keep_discharged_true_when_its_true() {
        Integer id =1;
        when(authorVORepository.getById(any(Integer.class))).thenReturn(Optional.of(authorsList.get(1)));
        AuthorVO authorVO = authorVORepository.getById(id).get();
        authorService.discharge(id);
        assertTrue(authorVO.getDischarged());
    }

}