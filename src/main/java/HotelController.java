import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TextFormatter;
import java.util.Locale;


public class HotelController {

    @FXML
    private TextField monMail;
    private String email;
    @FXML
    private TextField monMdp;
    private String mdp;

    @FXML
    private AnchorPane HOME;

    @FXML
    private Button ReserverButton;


    @FXML
    private AnchorPane INSCRIPTION;



    @FXML
    private TextField NOM;
    @FXML
    private TextField PRENOM;
    @FXML
    private DatePicker DATENAISSANCE;
    @FXML
    private TextField EMAIL;
    @FXML
    private TextField MDP;

    @FXML
    private RadioButton chambreClassique;
    @FXML
    private RadioButton chambreDouble;
    @FXML
    private RadioButton suiteVIP;

    @FXML
    private ImageView imageChambre;

    @FXML
    private Label prixChambre;

    @FXML
    private CheckBox petitDej;
    @FXML
    private CheckBox wifi;
    @FXML
    private CheckBox parking;
    @FXML
    private DatePicker dateArrivee;
    @FXML
    private DatePicker dateDepart;

    @FXML
    private ListView<Reservation> listeReservations;

    @FXML
    private Label prixTotalReservations;

    @FXML
    private Label prixTotalPaiement;

    @FXML
    private TabPane panneauReservation;

    @FXML
    private Tab pageSelection;

    @FXML
    private Tab pageInformations;

    @FXML
    private Tab pagePaiement;

    @FXML
    private TextField numCB, nomCB, dateCB, CVV;


    private Stage stage;

    @FXML
    private Pane TOUT;
    private boolean descendu=false;
    @FXML
    private Label donneesCompte ;
    @FXML
    private MenuItem donnees;
    @FXML
    private MenuButton profile;
    @FXML
    private Button inscription;

    @FXML
    private Button connexion;

    @FXML
    private Label texteModif;
    private boolean glisse = false;
    @FXML
    private Pane slide;
    @FXML
    private MenuItem infosModif;

    private int numReservation;
    private Reservation nouvelle;
    private Reservation ancien;

    @FXML
    private Label infosPerso;
    @FXML
    private CheckBox confirmInfos;

    @FXML
    private ListView<Reservation> allReservations;

    @FXML
    private TabPane panneauModification;

    @FXML
    private Tab pageSelectionModif;

    @FXML
    private CheckBox annulerReservation;
    @FXML
    private Tab pageModifier;
    @FXML
    private Tab pageRajout;
    @FXML
    private Tab pageRemboursement;

    @FXML
    private Label prixTotalRajout;
    @FXML
    private Label prixTotalRemboursement;
    @FXML
    private CheckBox validRIB;


    @FXML
    public void initialize() {

        if (profile != null) {
            if (Session.currentUser != null) {
                connexion.setVisible(false);
                inscription.setVisible(false);
                profile.setVisible(true);
            } else {
                connexion.setVisible(true);
                inscription.setVisible(true);
                profile.setVisible(false);
            }
        }

        if (numCB != null && nomCB != null && dateCB != null && CVV != null) {
            configurerChampsBancaires();
        }
    }

    @FXML
    private void validerConnexion(ActionEvent event) {
        email = monMail.getText();
        mdp = monMdp.getText();

        if(email.isBlank() || mdp.isBlank() || !ClientDataBase.exists(email,mdp)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Informations non valides");
            alert.setContentText("Entrez un mail et un mot de passe valide");
            alert.show();
            return;
        }


        Session.currentUser = ClientDataBase.login(email, mdp);
        Session.panier.listeReservations.clear();
        Home(event);
        UpdateInterface();

    }

    @FXML

    private void Reserver(ActionEvent event){
        /**
        if(Session.currentUser==null){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText("Veillez à vous connecter pour pouvoir réserver plus facilement");
            alert.show();
            return;
        }**/
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/reservation.fxml"));
            Scene inscrireScene = new Scene(loader.load());
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Reserver");
            stage.setScene(inscrireScene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @FXML
    private void Reservations(ActionEvent event){
        if(Session.currentUser == null){Alert alert= new Alert(Alert.AlertType.ERROR);alert.setHeaderText("Veuillez vous connecter");alert.show();return;}
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/mesReservations.fxml"));
            Scene inscrireScene = new Scene(loader.load());
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Mes Reservation");
            stage.setScene(inscrireScene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void Inscrire(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/inscription.fxml"));
            Scene inscrireScene = new Scene(loader.load());

            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setTitle("Inscription");
            stage.setScene(inscrireScene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void Connecter(ActionEvent event){

        try {
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/connexion.fxml"));
            Scene connectScene = new Scene(loader.load());
            Navigation.history.put("connect",connectScene);
            stage.setTitle("Connexion");
            stage.setScene(connectScene);
            stage.show();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    @FXML
    private void Home(ActionEvent event){

        try {
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/hotel.fxml"));
            Scene connectScene = new Scene(loader.load());
            Navigation.history.put("home",connectScene);
            stage.setTitle("ACCUEIL");
            stage.setScene(connectScene);
            stage.show();
        }
        catch (IOException e){
            e.printStackTrace();
        }


    }

    @FXML
    private void ConfirmerInscription(ActionEvent event){
        String nom= NOM.getText();
        String prenom= PRENOM.getText();
        String mail= EMAIL.getText();
        String password= MDP.getText();

        LocalDate dateanniv = DATENAISSANCE.getValue();

        if (nom.isBlank() || prenom.isBlank() || mail.isBlank()
                || password.isBlank() || dateanniv==null || ClientDataBase.existsMail(mail)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Informations non valides");
            if (ClientDataBase.existsMail(mail)){
                alert.setHeaderText("L'adresse mail est déjà utilisée");
            }
            else {
                alert.setHeaderText("Entrez des infos valides");
            }
            alert.show();
            return;
        }

        Client client = new Client(nom, prenom, mail, password, dateanniv);
        System.out.println(client);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Voici vos données");
        alert.setContentText(client.toString());
        alert.getDialogPane().setStyle("-fx-font-size: 12px;");
        alert.getDialogPane().setPrefWidth(600);
        alert.getDialogPane().setPrefHeight(300);
        alert.showAndWait();

        Home(event);

        ClientDataBase.addClient(client);
        if(ClientDataBase.exists(client.getEmail(), client.getPassword())) {
            Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
            alert2.setTitle("Informations enregistrées");
            alert2.setContentText("Vos informations ont été enregistrées avec succès:\n Il ne vous reste plus qu'à vous connecter et à prendre du plaisir à réserver");
            alert2.getDialogPane().setStyle("-fx-font-size: 12px;");
            alert2.getDialogPane().setPrefWidth(600);
            alert2.getDialogPane().setPrefHeight(300);
            alert2.show();
        }
        else{
            Alert alert3 = new Alert(Alert.AlertType.ERROR);
            alert3.setTitle("Ereur");
            alert3.setTitle("Le client n'a pas été créé.");
        }
    }

    private void UpdateInterface(){

        if (Session.currentUser != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Bienvenue");
            alert.setHeaderText("Connexion réussie");
            alert.setContentText("Bonjour " + Session.currentUser.prenom + " 👋\nBon séjour !");
            alert.showAndWait();

        }
    }

    @FXML
    private void afficherPrixClassique() {
        prixChambre.setText(Double.toString(Chambre.PRIX_CHAMBRE_CLASSIQUE));
        changerImageChambre("chambresimplee.jpg");
    }

    @FXML
    private void afficherPrixDouble() {
        prixChambre.setText(Double.toString(Chambre.PRIX_CHAMBRE_DOUBLE));
        changerImageChambre("chambredouble.jpg");
    }

    @FXML
    private void afficherPrixVIP() {
        prixChambre.setText(Double.toString(Chambre.PRIX_CHAMBRE_VIP));
        changerImageChambre("chambrevip.jpg");
    }

    private void changerImageChambre(String fichier) {
        var ressource = getClass().getResource("/images/" + fichier);

        if (ressource != null) {
            imageChambre.setImage(new Image(ressource.toExternalForm()));
        } else {
            System.err.println("Image introuvable : " + fichier);
        }
    }

    @FXML
    private void validerSelection(ActionEvent event){
        if(Session.currentUser==null){
            Alert alert2 = new Alert(Alert.AlertType.ERROR);
            alert2.setTitle("Erreur de connection");
            alert2.setHeaderText("Il faut se connecter");
            alert2.getDialogPane().setStyle("-fx-font-size: 12px;");
            alert2.getDialogPane().setPrefWidth(600);
            alert2.getDialogPane().setPrefHeight(300);
            alert2.showAndWait();
            Connecter(event);
            return;
        }
        LocalDate debut= dateArrivee.getValue();
        LocalDate fin= dateDepart.getValue();

        if (!debut.isBefore(fin) || debut==null || fin==null){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de dates");
            alert.setHeaderText("Il faut saisir des dates valides ( arrivée avant le départ )");
            alert.getDialogPane().setStyle("-fx-font-size: 12px;");
            alert.getDialogPane().setPrefWidth(600);
            alert.getDialogPane().setPrefHeight(300);
            alert.showAndWait();
            return;
        }
        String type;
        if(chambreClassique.isSelected()){type="CLASSIQUE";System.out.println(type);}
        else if(chambreDouble.isSelected()){type="DOUBLE";System.out.println(type);}
        else if(suiteVIP.isSelected()){type="VIP";System.out.println(type);}
        else {return;}

        int prixNuit = Chambre.TYPES.get(type);
        int options=0;
        String OPTIONS="";
        if(petitDej.isSelected()){options+=5;OPTIONS+=" : Petit Dejeuner : ";}
        if(wifi.isSelected()){options+=2;OPTIONS+=" : WiFi : ";}
        if(parking.isSelected()){options+=3;OPTIONS+=" : Parking : ";}

        int nbNuit=Reservation.getDuree(debut,fin);
        int total= (prixNuit+options)*nbNuit;
        if(Session.currentUser==Session.DEFAULT_USER){total=0;}
        Chambre chambre= new Chambre(type,total,OPTIONS);
        Reservation reservation = new Reservation(Session.currentUser,chambre,total,debut,fin,chambre.getOptions());
        System.out.println(chambre.toString());

        pageInformations.setDisable(false);
        panneauReservation.getSelectionModel().select(pageInformations);
        pageSelection.setDisable(true);

        if(!Session.panier.listeReservations.isEmpty()) {
            listeReservations.getItems().addAll(Session.panier.listeReservations);
        }
        Session.panier.listeReservations.add(reservation);
        listeReservations.setCellFactory(param -> new ListCell<Reservation>() {

            @Override
            protected void updateItem(Reservation item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Reservation n°" + (getIndex() + 1) + "\n" + item.toString());
                }
            }
        });
        listeReservations.getItems().add(reservation);
        prixTotalReservations.setText(String.valueOf(Session.panier.total())+" €");

    }

    @FXML
    private void validerInformations(ActionEvent event){
        pagePaiement.setDisable(false);
        panneauReservation.getSelectionModel().select(pagePaiement);
        pageInformations.setDisable(true);
        prixTotalPaiement.setText(String.valueOf(Session.panier.total())+" €");
        infosPerso.setText(Session.currentUser.toString());
    }

    @FXML
    private void retourPageSelection(ActionEvent event){
        pageSelection.setDisable(false);
        panneauReservation.getSelectionModel().select(pageSelection);
        pageInformations.setDisable(true);
    }

    @FXML
    private void validerPaiement(ActionEvent event){
        if(Session.panier.total()<=0 && Session.currentUser!=Session.DEFAULT_USER){
            Alert alert=new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("La commande est invalide");
            alert.show();
            return;
        }
        if(!confirmInfos.isSelected()){
            Alert alert=new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Veuillez confirmer vos infos");
            alert.show();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Êtes-vous sûr ?");

        ButtonType oui = new ButtonType("Oui");
        ButtonType non = new ButtonType("Non");

        alert.getButtonTypes().setAll(oui, non);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == non) {
            return;
        }
        pagePaiement.setDisable(true);
        for(Reservation r : Session.panier.listeReservations) {
            ReservationBataBase.addReservation(r);
        }
        TicketPDF.creerTicket(Session.panier);
        Session.panier.listeReservations.clear();
        Home(event);

    }

    private void configurerChampsBancaires() {

        // Numéro de carte : 19 chiffres maximum, espaces automatiques
        numCB.setTextFormatter(new TextFormatter<String>(change -> {
            if (!change.isContentChange()) return change;

            String chiffres = change.getControlNewText().replaceAll("\\D", "");

            if (chiffres.length() > 16) return null;

            String format = chiffres.replaceAll("(.{4})(?=.)", "$1 ");

            change.setRange(0, change.getControlText().length());
            change.setText(format);
            change.setCaretPosition(format.length());
            change.setAnchor(format.length());

            return change;
        }));

        // Titulaire : lettres, accents, espaces et majuscules
        nomCB.setTextFormatter(new TextFormatter<String>(change -> {
            String texte = change.getControlNewText();

            if (!texte.matches("[\\p{L} '\\-]*")) return null;

            change.setText(change.getText().toUpperCase(Locale.ROOT));

            return change;
        }));

        // Expiration : format MM/AA
        dateCB.setTextFormatter(new TextFormatter<String>(change -> {
            if (!change.isContentChange()) return change;

            String chiffres = change.getControlNewText().replaceAll("\\D", "");

            if (chiffres.length() > 4) return null;

            if (chiffres.length() >= 2) {
                int mois = Integer.parseInt(chiffres.substring(0, 2));

                if (mois < 1 || mois > 12) return null;
            }

            String format = chiffres.length() > 2
                    ? chiffres.substring(0, 2) + "/" + chiffres.substring(2)
                    : chiffres;

            change.setRange(0, change.getControlText().length());
            change.setText(format);
            change.setCaretPosition(format.length());
            change.setAnchor(format.length());

            return change;
        }));

        // CVV : 3 ou 4 chiffres maximum
        CVV.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d{0,3}") ? change : null
        ));
    }

    @FXML
    private void autoConnect(){Session.currentUser= Session.DEFAULT_USER;UpdateInterface();initialize();}

    @FXML
    private void deconnexion(){
        Session.currentUser= null;
        Session.panier.listeReservations.clear();
        initialize();
        Alert alert= new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Vous avez été déconnecté avec succés");
        alert.show();
    }


    @FXML
    private void mesDonnees(){
        donneesCompte.setText(Session.currentUser.toString());
        TranslateTransition tt = new TranslateTransition(Duration.millis(1000), TOUT);
        if(descendu==false){tt.setByY(200);descendu=true;donneesCompte.setVisible(true);donnees.setVisible(false);if(glisse==true){modifierInfos();}}
        else{tt.setByY(-200);descendu=false;donneesCompte.setVisible(false);donnees.setVisible(true);}

        tt.setInterpolator(Interpolator.EASE_BOTH);

        tt.play();
    }

    @FXML
    private void addLine(ActionEvent event){
        pageSelection.setDisable(false);
        panneauReservation.getSelectionModel().select(pageSelection);

    }

    @FXML
    private TextField lineNumber;

    @FXML
    private void deleteLine(){
        Reservation selected = listeReservations.getSelectionModel().getSelectedItem();
        /**
        int i;
        if (lineNumber.getText().isBlank()){Alert alert= new Alert(Alert.AlertType.INFORMATION);alert.setHeaderText("Entrez une ligne");alert.show(); return;}
        else{i=Integer.parseInt(lineNumber.getText())-1;}
        **/
        if (selected != null) {
            Session.panier.listeReservations.remove(selected);
            listeReservations.getItems().remove(selected);
            prixTotalReservations.setText(String.valueOf(Session.panier.total())+" €");
        }

        else {
            Alert alert= new Alert(Alert.AlertType.INFORMATION);alert.setHeaderText("Selectionnez une ligne");alert.show();
        }
    }

    @FXML
    private void retirerTout(){
        Session.panier.listeReservations.clear();
        listeReservations.getItems().clear();
        prixTotalReservations.setText("");
    }



    @FXML
    private void modifierInfos(){
        texteModif.setText(Session.currentUser.toString());
        TranslateTransition tt = new TranslateTransition(Duration.millis(1500), slide);
        if(glisse==false){tt.setByX(700);glisse=true;texteModif.setVisible(true);infosModif.setVisible(false);if(descendu==true){mesDonnees();}}
        else{tt.setByX(-700);glisse=false;texteModif.setVisible(false);infosModif.setVisible(true);}

        tt.setInterpolator(Interpolator.EASE_BOTH);

        tt.play();
    }

    private boolean fleches=false;
    @FXML
    private void imagesVilla(){
        TranslateTransition tt = new TranslateTransition(Duration.seconds(3), slide);
        if(fleches==false){tt.setByX(-650);fleches=true;if(descendu==true){mesDonnees();}}
        else{tt.setByX(650);fleches=false;}

        tt.setInterpolator(Interpolator.EASE_BOTH);

        tt.play();
    }


    @FXML

    private void afficherAllReservations(){
        Session.currentUser.mesReservations = ClientDataBase.getReservations(Session.currentUser);

        allReservations.setCellFactory(param -> new ListCell<Reservation>() {

            @Override
            protected void updateItem(Reservation item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Reservation n°" + (getIndex() + 1) + "\n" + item.toString());
                }
            }
        });

        for(Reservation reservation : Session.currentUser.mesReservations){
            allReservations.getItems().add(reservation);
        }

    }

    @FXML
    private void modifierReservation(){
        Reservation selected = allReservations.getSelectionModel().getSelectedItem();
        System.out.println(selected);
        Session.reservationAModifier=selected;
        pageModifier.setDisable(false);
        panneauModification.getSelectionModel().select(pageModifier);
        pageSelectionModif.setDisable(true);
        if(selected.getChambre().getType().equals("CLASSIQUE")){chambreClassique.setSelected(true);}
        else if(selected.getChambre().getType().equals("DOUBLE")){chambreDouble.setSelected(true);}
        else if(selected.getChambre().getType().equals("VIP")){suiteVIP.setSelected(true);}
        prixChambre.setText(String.valueOf(selected.getChambre().getPrix()));
        if(selected.getOptions().contains("Parking")) {parking.setSelected(true);}
        if(selected.getOptions().contains("Petit Dejeuner")){petitDej.setSelected(true);}
        if(selected.getOptions().contains("WiFi")){wifi.setSelected(true);}
        dateArrivee.setValue(selected.getDateDebut());
        dateDepart.setValue(selected.getDateFin());

    }

    @FXML
    private void retourSelectionModif(){
        pageSelectionModif.setDisable(false);
        panneauModification.getSelectionModel().select(pageSelectionModif);
        pageModifier.setDisable(true);
    }

    @FXML
    private void validerModifReservation(ActionEvent event){
        ancien=Session.reservationAModifier;
        if (annulerReservation.isSelected()){
            ReservationBataBase.deleteReservation(ReservationBataBase.getNum(ClientDataBase.getIdByMail(Session.currentUser.getEmail()),ancien.getChambre().getType(),ancien.getPrixTotal(),ancien.getDateDebut(),ancien.getDateFin(),ancien.getOptions()));
            Home(event);
            TicketPDF.reservationAnnulee= new Reservation(Session.currentUser,new Chambre("---",00,"---"),0,LocalDate.now(),LocalDate.now(),"---");
            TicketPDF.creerTicketRemboursement(ancien,TicketPDF.reservationAnnulee, ancien.getPrixTotal());
        }
        Chambre nvChambre = new Chambre(ancien.getChambre().getType(),ancien.getChambre().getPrix(),ancien.getChambre().getOptions());
        nouvelle= new Reservation(ancien.getClient(),nvChambre,ancien.getPrixTotal(),
                ancien.getDateDebut(),ancien.getDateFin(), ancien.getOptions());
        LocalDate debut= dateArrivee.getValue();
        LocalDate fin= dateDepart.getValue();
        if (!debut.isBefore(fin) || debut==null || fin==null){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de dates");
            alert.setHeaderText("Il faut saisir des dates valides ( arrivée avant le départ )");
            alert.getDialogPane().setStyle("-fx-font-size: 12px;");
            alert.getDialogPane().setPrefWidth(600);
            alert.getDialogPane().setPrefHeight(300);
            alert.showAndWait();
            return;
        }
        String type;
        if(chambreClassique.isSelected()){type="CLASSIQUE";System.out.println(type);}
        else if(chambreDouble.isSelected()){type="DOUBLE";System.out.println(type);}
        else if(suiteVIP.isSelected()){type="VIP";System.out.println(type);}
        else {return;}

        int prixNuit = Chambre.TYPES.get(type);
        int options=0;
        String OPTIONS="";
        if(petitDej.isSelected()){options+=5;OPTIONS+=" : Petit Dejeuner : ";}
        if(wifi.isSelected()){options+=2;OPTIONS+=" : WiFi : ";}
        if(parking.isSelected()){options+=3;OPTIONS+=" : Parking : ";}

        int nbNuit=Reservation.getDuree(debut,fin);
        int total= (prixNuit+options)*nbNuit;
        if(Session.currentUser==Session.DEFAULT_USER){total=0;}

        nouvelle.getChambre().setType(type);
        nouvelle.getChambre().setPrix(prixNuit);
        nouvelle.getChambre().setOptions(OPTIONS);
        nouvelle.setPrixTotal(total);
        nouvelle.setDateDebut(debut);
        nouvelle.setDateFin(fin);
        nouvelle.setOptions(nouvelle.getChambre().getOptions());

        System.out.println(nouvelle.toString());
        System.out.println(ancien.getChambre().getType());
        numReservation=ReservationBataBase.getNum(ClientDataBase.getIdByMail(ancien.getClient().getEmail()),ancien.getChambre().getType(), ancien.getPrixTotal(),ancien.getDateDebut(),ancien.getDateFin(),ancien.getOptions());
        System.out.println("ancien= "+ancien.getPrixTotal() + " nouveau= "+nouvelle.getPrixTotal());
        if(ancien.getPrixTotal()>nouvelle.getPrixTotal()){
            pageRemboursement.setDisable(false);
            pageRemboursement.setText("Remboursement");
            int a=ancien.getPrixTotal()-nouvelle.getPrixTotal();
            prixTotalRemboursement.setText(String.valueOf(a)+" €");
            panneauModification.getSelectionModel().select(pageRemboursement);
            pageModifier.setDisable(true);
        }
        else if(ancien.getPrixTotal()<nouvelle.getPrixTotal()){
            pageRajout.setDisable(false);
            pageRajout.setText("Rajout");
            int a=nouvelle.getPrixTotal()-ancien.getPrixTotal();
            prixTotalRajout.setText(String.valueOf(a)+" €");
            panneauModification.getSelectionModel().select(pageRajout);
            pageModifier.setDisable(true);
        }
        else {
            ReservationBataBase.updateReservation(numReservation,nouvelle);
            Home(event);
            TicketPDF.creerTicketModificationSimple(ancien,nouvelle);
        }

    }

    @FXML
    private void retourModif(){
        pageModifier.setDisable(false);
        panneauModification.getSelectionModel().select(pageModifier);
        pageRajout.setDisable(true);
        pageRajout.setText("");
        pageRemboursement.setDisable(true);
        pageRemboursement.setText("");
    }

    @FXML
    private void confirmRemboursement(ActionEvent event){
        int prix = ancien.getPrixTotal()-nouvelle.getPrixTotal();
        if(prix<=0 && Session.currentUser!=Session.DEFAULT_USER){
            Alert alert=new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("La commande est invalide mec");
            alert.show();
            return;
        }
        if(!validRIB.isSelected()){
            Alert alert=new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Veuillez valider le RIB");
            alert.show();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Êtes-vous sûr ?");

        ButtonType oui = new ButtonType("Oui");
        ButtonType non = new ButtonType("Non");

        alert.getButtonTypes().setAll(oui, non);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == non) {
            return;
        }
        pageRemboursement.setDisable(true);
        ReservationBataBase.updateReservation(numReservation,nouvelle);
        Home(event);
        TicketPDF.creerTicketRemboursement(ancien,nouvelle,prix);
    }

    @FXML
    private void confirmRajout(ActionEvent event){
        int prix = nouvelle.getPrixTotal()-ancien.getPrixTotal();
        if(prix<=0 && Session.currentUser!=Session.DEFAULT_USER){
            Alert alert=new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("La commande est invalide");
            alert.show();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Êtes-vous sûr ?");

        ButtonType oui = new ButtonType("Oui");
        ButtonType non = new ButtonType("Non");

        alert.getButtonTypes().setAll(oui, non);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == non) {
            return;
        }
        pageRajout.setDisable(true);
        ReservationBataBase.updateReservation(numReservation,nouvelle);
        Home(event);
        TicketPDF.creerTicketRajout(ancien,nouvelle,prix);
    }





}

