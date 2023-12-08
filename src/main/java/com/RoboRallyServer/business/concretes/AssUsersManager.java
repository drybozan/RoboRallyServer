package com.RoboRallyServer.business.concretes;

import com.RoboRallyServer.business.abstracts.AssUsersService;
import com.RoboRallyServer.dataAccess.abstracts.AssUsersDao;
import com.RoboRallyServer.entities.AssUsers;
import com.RoboRallyServer.utilities.results.DataResult;
import com.RoboRallyServer.utilities.results.ErrorDataResult;
import com.RoboRallyServer.utilities.results.SuccessDataResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssUsersManager implements AssUsersService {

    private final AssUsersDao assUsersDao;
    @Override
    public DataResult<AssUsers> login(AssUsers assUsers) {
        try {
            AssUsers user = this.assUsersDao.findByUsernameAndPassword(assUsers.getUsername(), assUsers.getPassword());

             if(user == null){

                 return new ErrorDataResult<>(user,"Hata ! Kullanıcı adı veya şifre hatalı.");
             }
            return new SuccessDataResult<>(user,user.getUsername() + " giriş yaptı");
        } catch (Exception e) {
            // Hata durumunda yapılacak işlemler...
            e.printStackTrace(); // Hata detaylarını konsola yazdırabilirsiniz.
            return new ErrorDataResult("Giriş başarısız. Hata: " + e.getMessage());
        }

    }
}
