
import ohtu.verkkokauppa.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class KauppaTest {

    Pankki pankki = mock(Pankki.class);
    Varasto varasto = mock(Varasto.class);
    Viitegeneraattori viite = mock(Viitegeneraattori.class);
    Kauppa kauppa;
    String ASIAKAS = "PEKKA";
    String TILI_NRO = "1234-1234";

    @Before
    public void setUp() {
        varasto = mock(Varasto.class);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "olut", 5));
        when(varasto.saldo(1)).thenReturn(2).thenReturn(1);
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "maito", 3));
        when(varasto.saldo(2)).thenReturn(1).thenReturn(0);

        viite = mock(Viitegeneraattori.class);
        when(viite.uusi()).
                thenReturn(7).
                thenReturn(8).
                thenReturn(9);

        pankki = mock(Pankki.class);
        kauppa = new Kauppa(varasto, pankki, viite);
    }

    @Test
    public void yhdenLisatynOstoksenLaskutusOikein() {
        // koriin lisätään koriin tuote jota varastossa on
        // kutsutaan pankin metodia tilisiirto oikealla asiakkaalla, tilinumerolla ja summalla
        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.tilimaksu(ASIAKAS, TILI_NRO);
        verify(pankki).tilisiirto(eq(ASIAKAS), anyInt(), eq(TILI_NRO), anyString(), eq(5));
    }

    @Test
    public void kahdenLisatynOstoksenLaskutusOikein() {
        // koriin lisätään koriin kaksi eri tuotetta joita varastossa on
        // varmistettava että kutsutaan pankin metodia tilisiirto oikealla asiakkaalla, tilinumerolla ja summalla
        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.lisaaKoriin(2);
        kauppa.tilimaksu(ASIAKAS, TILI_NRO);
        verify(pankki).tilisiirto(eq(ASIAKAS), anyInt(), eq(TILI_NRO), anyString(), eq(8));
    }

    @Test
    public void kahdenSamanLisatynOstoksenLaskutusOikein() {
        // koriin lisätään koriin kaksi samaa tuotetta jota on varastossa tarpeeksi
        // varmistettava että kutsutaan pankin metodia tilisiirto oikealla asiakkaalla, tilinumerolla ja summalla
        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.lisaaKoriin(1);
        kauppa.tilimaksu(ASIAKAS, TILI_NRO);
        verify(pankki).tilisiirto(eq(ASIAKAS), anyInt(), eq(TILI_NRO), anyString(), eq(10));
    }

    @Test
    public void tuotteenJonkaVarastosaldoNollaSisaltavanOstoksenLaskutusOikein() {
        // koriin lisätään koriin tuote jota on varastossa tarpeeksi ja tuote joka on loppu
        // varmistettava että kutsutaan pankin metodia tilisiirto oikealla asiakkaalla, tilinumerolla ja summalla
        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(2);
        kauppa.lisaaKoriin(2);
        kauppa.tilimaksu(ASIAKAS, TILI_NRO);
        verify(pankki).tilisiirto(eq(ASIAKAS), anyInt(), eq(TILI_NRO), anyString(), eq(3));
    }

    @Test
    public void aloitaAsiointiNollaaVanhanOstokenTiedotOstoksenLaskutusOikein() {
        // metodin aloita asiointi kutsuminen nollaa edellisen ostoksen tiedot
        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.lisaaKoriin(2);
        kauppa.tilimaksu(ASIAKAS, TILI_NRO);
        verify(pankki).tilisiirto(eq(ASIAKAS), anyInt(), eq(TILI_NRO), anyString(), eq(8));
    }

    @Test
    public void viitenumeroaPyydetaan() {
        // kauppa pyytää uuden viitenumeron jokaiselle maksutapahtumalle
        kauppa.aloitaAsiointi();
        kauppa.tilimaksu(ASIAKAS, TILI_NRO);
        verify(viite, times(1)).uusi();
        verify(pankki).tilisiirto(anyString(), eq(7), anyString(), anyString(), anyInt());

        kauppa.aloitaAsiointi();
        kauppa.tilimaksu(ASIAKAS, TILI_NRO);
        verify(viite, times(2)).uusi();
        verify(pankki).tilisiirto(anyString(), eq(8), anyString(), anyString(), anyInt());
        
        kauppa.aloitaAsiointi();
        kauppa.tilimaksu(ASIAKAS, TILI_NRO);
        verify(viite, times(3)).uusi();
        verify(pankki).tilisiirto(anyString(), eq(9), anyString(), anyString(), anyInt());        
    }
}
