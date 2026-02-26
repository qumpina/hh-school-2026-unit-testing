package ru.hh.school.unittesting.homework; // исправил пакет

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryManagerTest { // убрал модификатор доступа


  @Mock
  private NotificationService notificationService;

  @Mock
  private UserService userService;

  @InjectMocks
  private LibraryManager libraryManager;

  @BeforeEach
  void setUp() {//убрал лишнее создание libraryManager
    libraryManager.addBook("1", 15);
    libraryManager.addBook("2", 5);
  }

  @Test
  void testAddBookShouldIncreaseQuantityOfExistingBook() { // исправил опечатку
    libraryManager.addBook("1", 15);
    assertEquals(30, libraryManager.getAvailableCopies("1"));
  }

  @Test
  void testAddBookShouldAddBookWithNewQuantity() {
    libraryManager.addBook("3", 10);
    assertEquals(10, libraryManager.getAvailableCopies("3"));
  }

  @Test
  void testBorrowBookReturnTrueIfPositiveQuantityAndUserIsActive() {
    when(userService.isUserActive("1")).thenReturn(true);
    boolean borrowResult = libraryManager.borrowBook("2", "1");
    assertTrue(borrowResult);
    verify(notificationService).notifyUser("1", "You have borrowed the book: 2");
  }

  @Test
  void testBorrowBookShouldReturnFalseIfZeroOrNegativeQuantity() {
    when(userService.isUserActive("1")).thenReturn(true);
    boolean borrowResult = libraryManager.borrowBook("3", "1");
    assertFalse(borrowResult);
  }

  @Test
  void testBorrowBookShouldReturnFalseWhenUserAccountIsNotActive() {
    when(userService.isUserActive("1")).thenReturn(false);
    boolean borrowResult = libraryManager.borrowBook("2", "1");
    assertFalse(borrowResult);
    verify(notificationService).notifyUser("1", "Your account is not active.");
  }

  @Test
  void testReturnBookShouldReturnFalseIfNoSuchBookInInventory() {
    when(userService.isUserActive("1")).thenReturn(true);
    boolean result = libraryManager.borrowBook("0", "1");
    assertFalse(result);
  }

  @Test
  void testReturnBookShouldReturnFalseIfNoSuchBorrowedBook() { //Исправлено название теста
    boolean result = libraryManager.returnBook("2", "0");
    assertFalse(result);
  }

  @Test
  void testReturnBookShouldReturnFalseIfBookIsBorrowedButForAnotherUser() { // новый тест который покрывает случай, когда книгу возвращает не тот пользователь
    when(userService.isUserActive("0")).thenReturn(true);
    libraryManager.borrowBook("1", "0");
    boolean result = libraryManager.returnBook("1", "1");
    assertFalse(result);
  }

  @Test
  void testReturnBookShouldSucceedAndNotifyUser() {
    when(userService.isUserActive("1")).thenReturn(true);
    libraryManager.borrowBook("2", "1");
    boolean result = libraryManager.returnBook("2", "1");
    assertTrue(result);
    assertEquals(5, libraryManager.getAvailableCopies("2"));
    verify(notificationService).notifyUser("1", "You have returned the book: 2");
  }

  @Test
  void testGetAvailableCopiesForExistingBook() {
    libraryManager.addBook("5", 3);
    assertEquals(3, libraryManager.getAvailableCopies("5"));
  }

  @Test
  void testGetAvailableCopiesForNotExistingBook() {
    assertEquals(0, libraryManager.getAvailableCopies("0"));
  }

  @Test
  void testGetAvailableCopiesAfterBorrowingBook() {
    when(userService.isUserActive("1")).thenReturn(true);
    libraryManager.borrowBook("1", "1");
    assertEquals(14, libraryManager.getAvailableCopies("1"));
  }

  @Test
  void testCalculateDynamicLateFeeShouldThrowExceptionIfNoOverdue() {
    var exception = assertThrows(
        IllegalArgumentException.class,
        () -> libraryManager.calculateDynamicLateFee(-1, true, true));
    assertEquals("Overdue days cannot be negative.", exception.getMessage());
  }

  @ParameterizedTest
  @CsvSource({
      "3, false, false, 1.5",
      "5, true, false, 3.75",
      "10, true, true, 6",
      "4, false, true, 1.6",
      "0, true, true, 0" // добавил дополнительный тест на проверку при сдаче в срок

  })
  void testCalculateDynamicLateFee(int overdueDays, boolean isBestseller, boolean isPremiumMember, double expectedTotalPrice) {
    double totalPrice = libraryManager.calculateDynamicLateFee(overdueDays, isBestseller, isPremiumMember);
    assertEquals(expectedTotalPrice, totalPrice); // исправил название переменной

  }
}
